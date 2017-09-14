/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.ut.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.fraho.spring.securityJwt.config.*;
import eu.fraho.spring.securityJwt.dto.JwtUser;
import eu.fraho.spring.securityJwt.dto.RefreshToken;
import eu.fraho.spring.securityJwt.exceptions.FeatureNotConfiguredException;
import eu.fraho.spring.securityJwt.service.JwtTokenService;
import eu.fraho.spring.securityJwt.service.RefreshTokenStore;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Slf4j
@SuppressWarnings("unused")
public abstract class AbstractTestJwtTokenServiceWithRefresh extends TestJwtTokenService {
    public AbstractTestJwtTokenServiceWithRefresh() throws IOException {
        super();
    }

    @NotNull
    protected ObjectMapper getObjectMapper() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        return mapper;
    }

    @Before
    public void cleanupOldRefreshTokens() {
        getRefreshStore().revokeTokens();
    }

    @Test
    public void testGenerateRefreshToken() throws Exception {
        JwtTokenService service = getService();
        JwtUser user = getJwtUser();

        RefreshToken token1 = service.generateRefreshToken(user);
        Assert.assertNotNull("No token generated", token1.getToken());
        Assert.assertEquals("Wrong expiresIn", getRefreshConfig().getExpiration().toSeconds(), token1.getExpiresIn());

        RefreshToken token2 = service.generateRefreshToken(user);
        Assert.assertNotNull("No token generated", token2.getToken());
        Assert.assertNotEquals("No new toke ngenerated", token1, token2);
    }

    @Test
    public void testRefreshTokenSupported() throws Exception {
        JwtTokenService service = getService();

        Assert.assertTrue("Refresh tokens not supported", getRefreshStore().isRefreshTokenSupported());
        Assert.assertTrue("Refresh tokens not supported", service.isRefreshTokenSupported());
    }

    @Test
    public void testListRefreshTokensForUser() throws Exception {
        JwtTokenService service = getService();

        JwtUser jsmith = getJwtUser();
        jsmith.setUsername("jsmith");
        JwtUser xsmith = getJwtUser();
        xsmith.setUsername("xsmith");

        RefreshToken tokenA = service.generateRefreshToken(jsmith);
        RefreshToken tokenB = service.generateRefreshToken(jsmith);
        RefreshToken tokenC = service.generateRefreshToken(xsmith);

        final List<RefreshToken> jsmithTokens = service.listRefreshTokens(jsmith);
        Assert.assertTrue("Not all tokens returned", jsmithTokens.containsAll(Arrays.asList(tokenA, tokenB)));
        Assert.assertEquals("Unexpected token cound", 2, jsmithTokens.size());

        final List<RefreshToken> xsmithTokens = service.listRefreshTokens(xsmith);
        Assert.assertTrue("Not all tokens returned", xsmithTokens.contains(tokenC));
        Assert.assertEquals("Unexpected token cound", 1, xsmithTokens.size());
    }

    @Test(timeout = 10_000L)
    public void testExpireRefreshToken() throws Exception {
        JwtTokenService service = getService();
        JwtUser user = getJwtUser();

        RefreshToken token = service.generateRefreshToken(user);
        Assert.assertNotNull("No token generated", token.getToken());
        Thread.sleep(getRefreshConfig().getExpiration().toSeconds() * 1000 + 100);
        Assert.assertFalse("Token didn't expire", service.useRefreshToken(token.getToken()).isPresent());
    }

    @Test
    public void testListRefreshTokens() throws Exception {
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
        Assert.assertEquals("Token count don't match", 3, allTokens.size());
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

        Thread.sleep(getRefreshConfig().getExpiration().toSeconds() * 1000 + 100);

        final Map<Long, List<RefreshToken>> tokenMap = service.listRefreshTokens();
        Assert.assertEquals("User count don't match", 0, tokenMap.size());

        final List<RefreshToken> allTokens = tokenMap.values().stream().flatMap(Collection::stream)
                .collect(Collectors.toList());
        Assert.assertEquals("Token count don't match", 0, allTokens.size());
    }

    @Test
    public void testRemoveSingleToken() throws Exception {
        JwtTokenService service = getService();
        JwtUser user = getJwtUser();

        RefreshToken tokenA = service.generateRefreshToken(user);
        RefreshToken tokenB = service.generateRefreshToken(user);

        final List<RefreshToken> tokens = service.listRefreshTokens(user);
        Assert.assertEquals("Token count don't match", 2, tokens.size());

        Assert.assertTrue("Token should be revoked", service.revokeRefreshToken(tokenA));
        Assert.assertEquals("Token list is not immutable", 2, tokens.size());
        final List<RefreshToken> tokens2 = service.listRefreshTokens(user);
        Assert.assertEquals("Token was not revoked", 1, tokens2.size());
        Assert.assertTrue("Wrong token revoked", tokens2.contains(tokenB));
    }

    @Test
    public void testRemoveSingleTokenTwice() throws Exception {
        JwtTokenService service = getService();
        JwtUser jsmith = getJwtUser();

        RefreshToken tokenA = service.generateRefreshToken(jsmith);

        List<RefreshToken> tokens = service.listRefreshTokens(jsmith);
        Assert.assertEquals("Token count don't match", 1, tokens.size());

        Assert.assertTrue("Token should be revoked", service.revokeRefreshToken(tokenA));
        Assert.assertEquals("Token list is not immutable", 1, tokens.size());
        List<RefreshToken> tokens2 = service.listRefreshTokens(jsmith);
        Assert.assertEquals("Token count don't match", 0, tokens2.size());
        Assert.assertFalse("Token should be already revoked", service.revokeRefreshToken(tokenA));
    }

    @Test
    public void testRemoveAllTokensForUser() throws Exception {
        JwtTokenService service = getService();
        JwtUser jsmith = getJwtUser();

        service.generateRefreshToken(jsmith);
        service.generateRefreshToken(jsmith);

        final List<RefreshToken> tokens = service.listRefreshTokens(jsmith);
        Assert.assertEquals("Token count don't match", 2, tokens.size());

        Assert.assertEquals("Tokens should be revoked", 2, service.revokeRefreshTokens(jsmith));
        Assert.assertEquals("Token list is not immutable", 2, tokens.size());
        final List<RefreshToken> tokens2 = service.listRefreshTokens(jsmith);
        Assert.assertTrue("Tokens were not revoked", tokens2.isEmpty());
    }

    @Test
    public void testRemoveAllTokens() throws Exception {
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
        Assert.assertEquals("Token count don't match", 3, tokens.size());

        int count = service.clearTokens();
        Assert.assertEquals("Token list is not immutable", 3, tokens.size());
        final List<RefreshToken> tokens2 = service.listRefreshTokens(jsmith);
        Assert.assertTrue("Tokens were not revoked", tokens2.isEmpty());
        Assert.assertEquals("Revoked token count should be 3", 3, count);
    }

    @Test
    public void testUseRefreshTokenOnlyStrings() throws Exception {
        JwtTokenService service = getService();
        JwtUser jsmith = getJwtUser();

        RefreshToken tokenA = service.generateRefreshToken(jsmith);
        RefreshToken tokenB = service.generateRefreshToken(jsmith);

        final List<RefreshToken> tokens = service.listRefreshTokens(jsmith);
        Assert.assertEquals("Token count don't match", 2, tokens.size());

        Assert.assertTrue("Token should be used", service.useRefreshToken(tokenA.getToken()).isPresent());
        Assert.assertEquals("Wrong token used", tokenB, service.listRefreshTokens(jsmith).get(0));
    }

    @Test
    public void testUseRefreshToken() throws Exception {
        JwtTokenService service = getService();

        Assert.assertFalse("Unknown token used", service.useRefreshToken("baz").isPresent());
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testUseTokenNoPrivateKey() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getRsaTokenConfig();
        JwtTokenService service = getService(tokenConfiguration);

        try {
            Assert.assertEquals("RSA config should be in use", "RS256", tokenConfiguration.getAlgorithm());
            service.useRefreshToken("baz");
        } catch (FeatureNotConfiguredException fnce) {
            Assert.assertEquals("Wrong error message", "Access token signing is not enabled.", fnce.getMessage());
            throw fnce;
        }
    }

    @NotNull
    @Override
    protected abstract RefreshTokenStore getRefreshStore();
}
