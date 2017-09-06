/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.ut.service;

import eu.fraho.spring.securityJwt.config.JwtRefreshConfiguration;
import eu.fraho.spring.securityJwt.config.JwtTokenConfiguration;
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

    @Before
    public void cleanupOldRefreshTokens() {
        getRefreshStore().revokeTokens();
    }

    @Test
    public void testGenerateRefreshToken() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore, new JwtUser());

        RefreshToken token1 = service.generateRefreshToken("foo");
        Assert.assertNotNull("No token generated", token1.getToken());
        Assert.assertEquals("Wrong expiresIn", refreshConfiguration.getExpiration().toSeconds(), token1.getExpiresIn());

        RefreshToken token2 = service.generateRefreshToken("foo", "bar");
        Assert.assertNotNull("No token generated", token2.getToken());
        Assert.assertEquals("Custom deviceId ignored", "bar", token2.getDeviceId());
    }

    @Test
    public void testRefreshTokenSupported() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore, new JwtUser());

        Assert.assertTrue("Refresh tokens not supported", refreshTokenStore.isRefreshTokenSupported());
        Assert.assertTrue("Refresh tokens not supported", service.isRefreshTokenSupported());
    }

    @Test
    public void testListRefreshTokensForUser() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore, new JwtUser());

        String jsmith = "jsmith";
        String xsmith = "xsmith";

        RefreshToken tokenA = service.generateRefreshToken(jsmith);
        RefreshToken tokenB = service.generateRefreshToken(jsmith, "foobar");
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
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore, new JwtUser());

        String jsmith = "jsmith";
        RefreshToken token = service.generateRefreshToken(jsmith);
        Assert.assertNotNull("No token generated", token.getToken());
        Thread.sleep(refreshConfiguration.getExpiration().toSeconds() * 1000 + 100);
        Assert.assertFalse("Token didn't expire", service.useRefreshToken(jsmith, token));
    }

    @Test
    public void testListRefreshTokens() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore, new JwtUser());

        String jsmith = "jsmith";
        String xsmith = "xsmith";

        RefreshToken tokenA = service.generateRefreshToken(jsmith);
        RefreshToken tokenB = service.generateRefreshToken(jsmith, "foobar");
        RefreshToken tokenC = service.generateRefreshToken(xsmith, null);

        final Map<String, List<RefreshToken>> tokenMap = service.listRefreshTokens();
        Assert.assertEquals("User count don't match", 2, tokenMap.size());

        final List<RefreshToken> allTokens = tokenMap.values().stream().flatMap(Collection::stream)
                .collect(Collectors.toList());
        Assert.assertEquals("Token count don't match", 3, allTokens.size());
        Assert.assertTrue("Not all tokens returned", allTokens.containsAll(Arrays.asList(tokenA, tokenB, tokenC)));
    }

    @Test(timeout = 10_000L)
    public void testListRefreshTokensExpiration() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore, new JwtUser());

        String jsmith = "jsmith";
        String xsmith = "xsmith";

        service.generateRefreshToken(jsmith);
        service.generateRefreshToken(jsmith, "foobar");
        service.generateRefreshToken(xsmith);

        Thread.sleep(refreshConfiguration.getExpiration().toSeconds() * 1000 + 100);

        final Map<String, List<RefreshToken>> tokenMap = service.listRefreshTokens();
        Assert.assertEquals("User count don't match", 0, tokenMap.size());

        final List<RefreshToken> allTokens = tokenMap.values().stream().flatMap(Collection::stream)
                .collect(Collectors.toList());
        Assert.assertEquals("Token count don't match", 0, allTokens.size());
    }

    @Test
    public void testRemoveSingleToken() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore, new JwtUser());

        String jsmith = "jsmith";

        RefreshToken tokenA = service.generateRefreshToken(jsmith);
        RefreshToken tokenB = service.generateRefreshToken(jsmith, "foobar");

        final List<RefreshToken> tokens = service.listRefreshTokens(jsmith);
        Assert.assertEquals("Token count don't match", 2, tokens.size());

        Assert.assertTrue("Token should be revoked", service.revokeRefreshToken(jsmith, tokenA));
        Assert.assertEquals("Token list is not immutable", 2, tokens.size());
        final List<RefreshToken> tokens2 = service.listRefreshTokens(jsmith);
        Assert.assertEquals("Token was not revoked", 1, tokens2.size());
        Assert.assertTrue("Wrong token revoked", tokens2.contains(tokenB));
    }

    @Test
    public void testRemoveSingleTokenOtherWay() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore, new JwtUser());

        String jsmith = "jsmith";

        RefreshToken tokenA = service.generateRefreshToken(jsmith, "foo");
        RefreshToken tokenB = service.generateRefreshToken(jsmith);

        final List<RefreshToken> tokens = service.listRefreshTokens(jsmith);
        Assert.assertEquals("Token count don't match", 2, tokens.size());

        Assert.assertTrue("Token should be revoked", service.revokeRefreshToken(jsmith, tokenA.getDeviceId()));
        Assert.assertEquals("Token list is not immutable", 2, tokens.size());
        final List<RefreshToken> tokens2 = service.listRefreshTokens(jsmith);
        Assert.assertEquals("Token was not revoked", 1, tokens2.size());
        Assert.assertTrue("Wrong token revoked", tokens2.contains(tokenB));
        Assert.assertTrue("Token should be revoked", service.revokeRefreshToken(jsmith, tokenB.getDeviceId()));
    }

    @Test
    public void testRemoveSingleTokenTwice() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore, new JwtUser());

        String jsmith = "jsmith";

        RefreshToken tokenA = service.generateRefreshToken(jsmith);

        List<RefreshToken> tokens = service.listRefreshTokens(jsmith);
        Assert.assertEquals("Token count don't match", 1, tokens.size());

        Assert.assertTrue("Token should be revoked", service.revokeRefreshToken(jsmith, tokenA));
        Assert.assertEquals("Token list is not immutable", 1, tokens.size());
        List<RefreshToken> tokens2 = service.listRefreshTokens(jsmith);
        Assert.assertEquals("Token count don't match", 0, tokens2.size());
        Assert.assertFalse("Token should be already revoked", service.revokeRefreshToken(jsmith, tokenA));
    }

    @Test
    public void testRemoveAllTokensForUser() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore, new JwtUser());

        String jsmith = "jsmith";

        service.generateRefreshToken(jsmith);
        service.generateRefreshToken(jsmith, "foobar");

        final List<RefreshToken> tokens = service.listRefreshTokens(jsmith);
        Assert.assertEquals("Token count don't match", 2, tokens.size());

        Assert.assertEquals("Tokens should be revoked", 2, service.revokeRefreshTokens(jsmith));
        Assert.assertEquals("Token list is not immutable", 2, tokens.size());
        final List<RefreshToken> tokens2 = service.listRefreshTokens(jsmith);
        Assert.assertTrue("Tokens were not revoked", tokens2.isEmpty());
    }

    @Test
    public void testRemoveAllTokens() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore, new JwtUser());

        String jsmith = "jsmith";
        String xsmith = "xsmith";

        service.generateRefreshToken(jsmith);
        service.generateRefreshToken(jsmith, "foobar");
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
    public void testRemoveTokenByUserAndDeviceId() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore, new JwtUser());

        String jsmith = "jsmith";

        RefreshToken tokenA = service.generateRefreshToken(jsmith);
        RefreshToken tokenB = service.generateRefreshToken(jsmith, "foobar");

        final List<RefreshToken> tokens = service.listRefreshTokens(jsmith);
        Assert.assertEquals("Token count don't match", 2, tokens.size());

        service.revokeRefreshToken("jsmith", tokenB.getDeviceId());
        Assert.assertEquals("Token list is not immutable", 2, tokens.size());
        final List<RefreshToken> tokens2 = service.listRefreshTokens(jsmith);
        Assert.assertEquals("Token was not revoked", 1, tokens2.size());
        Assert.assertTrue("Wrong token revoked", tokens2.contains(tokenA));
    }

    @Test
    public void testRemoveSingleTokenByName() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore, new JwtUser());

        String jsmith = "jsmith";

        RefreshToken tokenA = service.generateRefreshToken(jsmith);
        service.generateRefreshToken(jsmith, "foobar");

        final List<RefreshToken> tokens = service.listRefreshTokens(jsmith);
        Assert.assertEquals("Token count don't match", 2, tokens.size());

        Assert.assertTrue("Token should be revoked", service.revokeRefreshToken(jsmith, "foobar"));
        Assert.assertEquals("Token list is not immutable", 2, tokens.size());
        final List<RefreshToken> tokens2 = service.listRefreshTokens(jsmith);
        Assert.assertEquals("Token was not revoked", 1, tokens2.size());
        Assert.assertTrue("Wrong token revoked", tokens2.contains(tokenA));
    }

    @Test
    public void testUseRefreshTokenOnlyStrings() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore, new JwtUser());

        String jsmith = "jsmith";

        RefreshToken tokenA = service.generateRefreshToken(jsmith);
        RefreshToken tokenB = service.generateRefreshToken(jsmith, "foobar");

        final List<RefreshToken> tokens = service.listRefreshTokens(jsmith);
        Assert.assertEquals("Token count don't match", 2, tokens.size());

        Assert.assertTrue("Token should be used", service.useRefreshToken(jsmith, tokenA.getToken()));
        Assert.assertEquals("Wrong token used", tokenB, service.listRefreshTokens(jsmith).get(0));
    }

    @Test
    public void testUseRefreshToken() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore, new JwtUser());

        Assert.assertFalse("Unknown token used", service.useRefreshToken("foo", "bar", "baz"));
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testUseTokenNoPrivateKey() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getRsaTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore, new JwtUser());

        try {
            service.useRefreshToken("foo", "bar", "baz");
        } catch (FeatureNotConfiguredException fnce) {
            Assert.assertEquals("Wrong error message", "Access token signing is not enabled.", fnce.getMessage());
            throw fnce;
        }
    }

    @NotNull
    @Override
    protected abstract RefreshTokenStore getRefreshStore();
}
