/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.ut.service;

import eu.fraho.spring.securityJwt.dto.JwtUser;
import eu.fraho.spring.securityJwt.exceptions.FeatureNotConfiguredException;
import eu.fraho.spring.securityJwt.service.NullTokenStore;
import eu.fraho.spring.securityJwt.service.RefreshTokenStore;
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
    public void testSaveToken() throws Exception {
        getNewInstance().saveToken(getJwtUser(), "bar");
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testUseToken() throws Exception {
        getNewInstance().useToken("bar");
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testListTokens() throws Exception {
        getNewInstance().listTokens();
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testListTokensForUser() throws Exception {
        getNewInstance().listTokens(getJwtUser());
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testRevokeToken() throws Exception {
        getNewInstance().revokeToken("baz");
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testRevokeTokens() throws Exception {
        getNewInstance().revokeTokens();
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testRevokeTokensForUser() throws Exception {
        getNewInstance().revokeTokens(getJwtUser());
    }

    @Test
    public void testIsRefreshTokenSupported() {
        Assert.assertFalse(getNewInstance().isRefreshTokenSupported());
    }
}