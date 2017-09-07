/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.ut.service;

import eu.fraho.spring.securityJwt.dto.RefreshToken;
import eu.fraho.spring.securityJwt.exceptions.FeatureNotConfiguredException;
import eu.fraho.spring.securityJwt.service.NullTokenStore;
import eu.fraho.spring.securityJwt.service.RefreshTokenStore;
import org.junit.Assert;
import org.junit.Test;

public class NullTokenStoreTest {
    private RefreshTokenStore getNewInstance() {
        NullTokenStore tokenStore = new NullTokenStore();
        tokenStore.afterPropertiesSet();
        return tokenStore;
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testSaveToken() throws Exception {
        getNewInstance().saveToken("foo", "bar", "baz");
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testUseToken() throws Exception {
        getNewInstance().useToken("foo", "bar", "baz");
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testListTokens() throws Exception {
        getNewInstance().listTokens();
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testListTokensForUser() throws Exception {
        getNewInstance().listTokens("foo");
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testRevokeTokenUserAndDevice() throws Exception {
        getNewInstance().revokeToken("foo", "bar");
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testRevokeTokenUserAndToken() throws Exception {
        getNewInstance().revokeToken("foo", new RefreshToken("baz", 0, "bar"));
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testRevokeTokens() throws Exception {
        getNewInstance().revokeTokens();
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testRevokeTokensForUser() throws Exception {
        getNewInstance().revokeTokens("foo");
    }

    @Test(expected = FeatureNotConfiguredException.class)
    @Deprecated
    public void testGetRefreshExpiration() throws Exception {
        getNewInstance().getRefreshExpiration();
    }

    @Test
    public void testIsRefreshTokenSupported() {
        Assert.assertFalse(getNewInstance().isRefreshTokenSupported());
    }
}