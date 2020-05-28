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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
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

    @Before
    public void cleanupOldRefreshTokens() {
        getRefreshStore().revokeTokens();
    }

    @Test
    public void testRefreshTokenSupported() {
        JwtTokenService service = getService();

        Assert.assertTrue("Refresh tokens not supported", getRefreshStore().isRefreshTokenSupported());
        Assert.assertTrue("Refresh tokens not supported", service.isRefreshTokenSupported());
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
        Assert.assertTrue("Contains tokenA", jsmithTokens.contains(tokenA));
        Assert.assertTrue("Contains tokenB", jsmithTokens.contains(tokenB));
        Assert.assertEquals("Unexpected token count", 2, jsmithTokens.size());

        final List<RefreshToken> xsmithTokens = service.listRefreshTokens(xsmith);
        Assert.assertTrue("Contains tokenC", xsmithTokens.contains(tokenC));
        Assert.assertEquals("Unexpected token count", 1, xsmithTokens.size());
    }

    @Test(timeout = 10_000L)
    public void testExpireRefreshToken() throws Exception {
        JwtTokenService service = getService();
        JwtUser user = getJwtUser();

        RefreshToken token = service.generateRefreshToken(user);
        Assert.assertNotNull("No token generated", token.getToken());
        Thread.sleep(getRefreshProperties().getExpiration().toSeconds() * 1000 + 100);
        Assert.assertFalse("AbstractToken didn't expire", service.useRefreshToken(token.getToken()).isPresent());
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
        Assert.assertEquals("User count don't match", 2, tokenMap.size());

        final List<RefreshToken> allTokens = tokenMap.values().stream().flatMap(Collection::stream)
                .collect(Collectors.toList());
        Assert.assertEquals("RefreshToken count don't match", 3, allTokens.size());
        Assert.assertTrue("Not all tokens returned", allTokens.containsAll(Arrays.asList(tokenA, tokenB, tokenC)));
    }

    @Test(timeout = 10_000L)
    public void testListRefreshTokensExpiration() throws Exception {
        JwtTokenService service = getService();

        JwtUser jsmith = getJwtUser();
        jsmith.setUsername("jsmith");
        JwtUser xsmith = getJwtUser();
        xsmith.setUsername("xsmith");

        service.generateRefreshToken(jsmith);
        service.generateRefreshToken(jsmith);
        service.generateRefreshToken(xsmith);

        Thread.sleep(getRefreshProperties().getExpiration().toSeconds() * 1000 + 100);

        final Map<Long, List<RefreshToken>> tokenMap = service.listRefreshTokens();
        Assert.assertEquals("User count don't match", 0, tokenMap.size());

        final List<RefreshToken> allTokens = tokenMap.values().stream().flatMap(Collection::stream)
                .collect(Collectors.toList());
        Assert.assertEquals("RefreshToken count don't match", 0, allTokens.size());
    }

    @Test
    public void testRemoveSingleToken() {
        JwtTokenService service = getService();
        JwtUser user = getJwtUser();

        RefreshToken tokenA = service.generateRefreshToken(user);
        RefreshToken tokenB = service.generateRefreshToken(user);

        final List<RefreshToken> tokens = service.listRefreshTokens(user);
        Assert.assertEquals("RefreshToken count don't match", 2, tokens.size());

        Assert.assertTrue("AbstractToken should be revoked", service.revokeRefreshToken(tokenA));
        Assert.assertEquals("AbstractToken list is not immutable", 2, tokens.size());
        final List<RefreshToken> tokens2 = service.listRefreshTokens(user);
        Assert.assertEquals("AbstractToken was not revoked", 1, tokens2.size());
        Assert.assertTrue("Wrong token revoked", tokens2.contains(tokenB));
    }

    @Test
    public void testRemoveSingleTokenTwice() {
        JwtTokenService service = getService();
        JwtUser jsmith = getJwtUser();

        RefreshToken tokenA = service.generateRefreshToken(jsmith);

        List<RefreshToken> tokens = service.listRefreshTokens(jsmith);
        Assert.assertEquals("RefreshToken count don't match", 1, tokens.size());

        Assert.assertTrue("AbstractToken should be revoked", service.revokeRefreshToken(tokenA));
        Assert.assertEquals("AbstractToken list is not immutable", 1, tokens.size());
        List<RefreshToken> tokens2 = service.listRefreshTokens(jsmith);
        Assert.assertEquals("RefreshToken count don't match", 0, tokens2.size());
        Assert.assertFalse("AbstractToken should be already revoked", service.revokeRefreshToken(tokenA));
    }

    @Test
    public void testRemoveAllTokensForUser() {
        JwtTokenService service = getService();
        JwtUser jsmith = getJwtUser();

        service.generateRefreshToken(jsmith);
        service.generateRefreshToken(jsmith);

        final List<RefreshToken> tokens = service.listRefreshTokens(jsmith);
        Assert.assertEquals("RefreshToken count don't match", 2, tokens.size());

        Assert.assertEquals("Tokens should be revoked", 2, service.revokeRefreshTokens(jsmith));
        Assert.assertEquals("AbstractToken list is not immutable", 2, tokens.size());
        final List<RefreshToken> tokens2 = service.listRefreshTokens(jsmith);
        Assert.assertTrue("Tokens were not revoked", tokens2.isEmpty());
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
        Assert.assertEquals("RefreshToken count don't match", 3, tokens.size());

        int count = service.clearTokens();
        Assert.assertEquals("AbstractToken list is not immutable", 3, tokens.size());
        final List<RefreshToken> tokens2 = service.listRefreshTokens(jsmith);
        Assert.assertTrue("Tokens were not revoked", tokens2.isEmpty());
        Assert.assertEquals("Revoked token count should be 3", 3, count);
    }

    @Test
    public void testUseRefreshTokenOnlyStrings() {
        JwtTokenService service = getService();
        JwtUser jsmith = getJwtUser();

        RefreshToken tokenA = service.generateRefreshToken(jsmith);
        RefreshToken tokenB = service.generateRefreshToken(jsmith);

        final List<RefreshToken> tokens = service.listRefreshTokens(jsmith);
        Assert.assertEquals("RefreshToken count don't match", 2, tokens.size());

        Assert.assertTrue("AbstractToken should be used", service.useRefreshToken(tokenA.getToken()).isPresent());
        final List<RefreshToken> tokensRead = service.listRefreshTokens(jsmith);
        Assert.assertEquals("RefreshToken count don't match", 1, tokensRead.size());
        Assert.assertEquals("Wrong token used", tokenB, tokensRead.get(0));
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testUseTokenNoPrivateKey() {
        TokenProperties tokenProperties = getRsaTokenProperties();
        JwtTokenService service = getService(tokenProperties);

        try {
            Assert.assertEquals("RSA config should be in use", "RS256", tokenProperties.getAlgorithm());
            service.useRefreshToken("baz");
        } catch (FeatureNotConfiguredException fnce) {
            Assert.assertEquals("Wrong error message", "Access token signing is not enabled.", fnce.getMessage());
            throw fnce;
        }
    }

    @Override
    protected abstract RefreshTokenStore getRefreshStore();
}
