/*
 * MIT Licence
 * Copyright (c) 2020 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.ut.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.fraho.spring.securityJwt.base.config.TokenProperties;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import eu.fraho.spring.securityJwt.base.dto.RefreshToken;
import eu.fraho.spring.securityJwt.base.exceptions.FeatureNotConfiguredException;
import eu.fraho.spring.securityJwt.base.service.JwtTokenService;
import eu.fraho.spring.securityJwt.base.service.RefreshTokenStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractJwtTokenServiceWithRefreshTest extends AbstractJwtTokenServiceTest {
    public AbstractJwtTokenServiceWithRefreshTest() throws IOException {
        super();
    }

    protected ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        return mapper;
    }

    @BeforeEach
    public void cleanupOldRefreshTokens() {
        getRefreshStore().revokeTokens();
    }

    @Test
    public void testRefreshTokenSupported() {
        JwtTokenService service = getService();

        Assertions.assertTrue(getRefreshStore().isRefreshTokenSupported(), "Refresh tokens not supported");
        Assertions.assertTrue(service.isRefreshTokenSupported(), "Refresh tokens not supported");
    }

    @Test
    public void testListRefreshTokensForUser() {
        JwtTokenService service = getService();

        JwtUser jsmith = getJwtUser();
        jsmith.setUsername("jsmith");
        JwtUser xsmith = getJwtUser();
        xsmith.setUsername("xsmith");

        RefreshToken tokenA = service.generateRefreshToken(jsmith);
        RefreshToken tokenB = service.generateRefreshToken(jsmith);
        RefreshToken tokenC = service.generateRefreshToken(xsmith);

        final List<RefreshToken> jsmithTokens = service.listRefreshTokens(jsmith);
        Assertions.assertTrue(jsmithTokens.contains(tokenA), "Contains tokenA");
        Assertions.assertTrue(jsmithTokens.contains(tokenB), "Contains tokenB");
        Assertions.assertEquals(2, jsmithTokens.size(), "Unexpected token count");

        final List<RefreshToken> xsmithTokens = service.listRefreshTokens(xsmith);
        Assertions.assertTrue(xsmithTokens.contains(tokenC), "Contains tokenC");
        Assertions.assertEquals(1, xsmithTokens.size(), "Unexpected token count");
    }

    @Test
    public void testExpireRefreshToken() throws Exception {
        JwtTokenService service = getService();
        JwtUser user = getJwtUser();

        RefreshToken token = Assertions.assertTimeout(Duration.ofSeconds(1), () -> service.generateRefreshToken(user));
        Assertions.assertNotNull(token.getToken(), "No token generated");
        Thread.sleep(getRefreshProperties().getExpiration().toSeconds() * 1000 + 100);
        Assertions.assertFalse(service.useRefreshToken(token.getToken()).isPresent(), "AbstractToken didn't expire");
    }

    @Test
    public void testListRefreshTokens() {
        JwtTokenService service = getService();

        JwtUser jsmith = getJwtUser();
        jsmith.setUsername("jsmith");
        JwtUser xsmith = getJwtUser();
        xsmith.setUsername("xsmith");

        RefreshToken tokenA = service.generateRefreshToken(jsmith);
        RefreshToken tokenB = service.generateRefreshToken(jsmith);
        RefreshToken tokenC = service.generateRefreshToken(xsmith);

        final Map<Long, List<RefreshToken>> tokenMap = service.listRefreshTokens();
        Assertions.assertEquals(2, tokenMap.size(), "User count don't match");

        final List<RefreshToken> allTokens = tokenMap.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        Assertions.assertEquals(3, allTokens.size(), "RefreshToken count don't match");
        Assertions.assertTrue(allTokens.containsAll(Arrays.asList(tokenA, tokenB, tokenC)), "Not all tokens returned");
    }

    @Test
    public void testListRefreshTokensExpiration() throws Exception {
        JwtTokenService service = getService();

        JwtUser jsmith = getJwtUser();
        jsmith.setUsername("jsmith");
        JwtUser xsmith = getJwtUser();
        xsmith.setUsername("xsmith");

        Assertions.assertTimeout(Duration.ofSeconds(1), () -> {
            service.generateRefreshToken(jsmith);
            service.generateRefreshToken(jsmith);
            service.generateRefreshToken(xsmith);
        });

        Thread.sleep(getRefreshProperties().getExpiration().toSeconds() * 1000 + 100);

        final Map<Long, List<RefreshToken>> tokenMap = service.listRefreshTokens();
        Assertions.assertEquals(0, tokenMap.size(), "User count don't match");

        final List<RefreshToken> allTokens = tokenMap.values().stream().flatMap(Collection::stream)
                .collect(Collectors.toList());
        Assertions.assertEquals(0, allTokens.size(), "RefreshToken count don't match");
    }

    @Test
    public void testRemoveSingleToken() {
        JwtTokenService service = getService();
        JwtUser user = getJwtUser();

        RefreshToken tokenA = service.generateRefreshToken(user);
        RefreshToken tokenB = service.generateRefreshToken(user);

        final List<RefreshToken> tokens = service.listRefreshTokens(user);
        Assertions.assertEquals(2, tokens.size(), "RefreshToken count don't match");

        Assertions.assertTrue(service.revokeRefreshToken(tokenA), "AbstractToken should be revoked");
        Assertions.assertEquals(2, tokens.size(), "AbstractToken list is not immutable");
        final List<RefreshToken> tokens2 = service.listRefreshTokens(user);
        Assertions.assertEquals(1, tokens2.size(), "AbstractToken was not revoked");
        Assertions.assertTrue(tokens2.contains(tokenB), "Wrong token revoked");
    }

    @Test
    public void testRemoveSingleTokenTwice() {
        JwtTokenService service = getService();
        JwtUser jsmith = getJwtUser();

        RefreshToken tokenA = service.generateRefreshToken(jsmith);

        List<RefreshToken> tokens = service.listRefreshTokens(jsmith);
        Assertions.assertEquals(1, tokens.size(), "RefreshToken count don't match");

        Assertions.assertTrue(service.revokeRefreshToken(tokenA), "AbstractToken should be revoked");
        Assertions.assertEquals(1, tokens.size(), "AbstractToken list is not immutable");
        List<RefreshToken> tokens2 = service.listRefreshTokens(jsmith);
        Assertions.assertEquals(0, tokens2.size(), "RefreshToken count don't match");
        Assertions.assertFalse(service.revokeRefreshToken(tokenA), "AbstractToken should be already revoked");
    }

    @Test
    public void testRemoveAllTokensForUser() {
        JwtTokenService service = getService();
        JwtUser jsmith = getJwtUser();

        service.generateRefreshToken(jsmith);
        service.generateRefreshToken(jsmith);

        final List<RefreshToken> tokens = service.listRefreshTokens(jsmith);
        Assertions.assertEquals(2, tokens.size(), "RefreshToken count don't match");

        Assertions.assertEquals(2, service.revokeRefreshTokens(jsmith), "Tokens should be revoked");
        Assertions.assertEquals(2, tokens.size(), "AbstractToken list is not immutable");
        final List<RefreshToken> tokens2 = service.listRefreshTokens(jsmith);
        Assertions.assertTrue(tokens2.isEmpty(), "Tokens were not revoked");
    }

    @Test
    public void testRemoveAllTokens() {
        JwtTokenService service = getService();

        JwtUser jsmith = getJwtUser();
        jsmith.setUsername("jsmith");
        JwtUser xsmith = getJwtUser();
        xsmith.setUsername("xsmith");

        service.generateRefreshToken(jsmith);
        service.generateRefreshToken(jsmith);
        service.generateRefreshToken(xsmith);

        final List<RefreshToken> tokens = service.listRefreshTokens().values().stream().flatMap(Collection::stream)
                .collect(Collectors.toList());
        Assertions.assertEquals(3, tokens.size(), "RefreshToken count don't match");

        int count = service.clearTokens();
        Assertions.assertEquals(3, tokens.size(), "AbstractToken list is not immutable");
        final List<RefreshToken> tokens2 = service.listRefreshTokens(jsmith);
        Assertions.assertTrue(tokens2.isEmpty(), "Tokens were not revoked");
        Assertions.assertEquals(3, count, "Revoked token count should be 3");
    }

    @Test
    public void testUseRefreshTokenOnlyStrings() {
        JwtTokenService service = getService();
        JwtUser jsmith = getJwtUser();

        RefreshToken tokenA = service.generateRefreshToken(jsmith);
        RefreshToken tokenB = service.generateRefreshToken(jsmith);

        final List<RefreshToken> tokens = service.listRefreshTokens(jsmith);
        Assertions.assertEquals(2, tokens.size(), "RefreshToken count don't match");

        Assertions.assertTrue(service.useRefreshToken(tokenA.getToken()).isPresent(), "AbstractToken should be used");
        final List<RefreshToken> tokensRead = service.listRefreshTokens(jsmith);
        Assertions.assertEquals(1, tokensRead.size(), "RefreshToken count don't match");
        Assertions.assertEquals(tokenB, tokensRead.get(0), "Wrong token used");
    }

    @Test
    public void testUseTokenNoPrivateKey() {
        TokenProperties tokenProperties = getRsaTokenProperties();
        JwtTokenService service = getService(tokenProperties);

        Assertions.assertEquals("RS256", tokenProperties.getAlgorithm(), "RSA config should be in use");
        Assertions.assertThrows(FeatureNotConfiguredException.class, () -> {
            service.useRefreshToken("baz");
        });
    }

    @Override
    protected abstract RefreshTokenStore getRefreshStore();
}
