/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.ut.service;

import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import eu.fraho.spring.securityJwt.base.exceptions.FeatureNotConfiguredException;
import eu.fraho.spring.securityJwt.base.service.NullTokenStore;
import eu.fraho.spring.securityJwt.base.service.RefreshTokenStore;
import org.junit.Assert;
import org.junit.Test;

public class NullTokenStoreTest {
    private JwtUser getJwtUser() {
        JwtUser user = new JwtUser();
        user.setId(42L);
        user.setUsername("foobar");
        return user;
    }

    private RefreshTokenStore getNewInstance() {
        NullTokenStore tokenStore = new NullTokenStore();
        tokenStore.afterPropertiesSet();
        return tokenStore;
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testSaveToken() {
        getNewInstance().saveToken(getJwtUser(), "bar");
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testUseToken() {
        getNewInstance().useToken("bar");
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testListTokens() {
        getNewInstance().listTokens();
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testListTokensForUser() {
        getNewInstance().listTokens(getJwtUser());
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testRevokeToken() {
        getNewInstance().revokeToken("baz");
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testRevokeTokens() {
        getNewInstance().revokeTokens();
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testRevokeTokensForUser() {
        getNewInstance().revokeTokens(getJwtUser());
    }

    @Test
    public void testIsRefreshTokenSupported() {
        Assert.assertFalse(getNewInstance().isRefreshTokenSupported());
    }
}