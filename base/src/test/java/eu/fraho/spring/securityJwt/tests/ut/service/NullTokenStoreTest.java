/*
 * MIT Licence
 * Copyright (c) 2025 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.tests.ut.service;

import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import eu.fraho.spring.securityJwt.base.exceptions.FeatureNotConfiguredException;
import eu.fraho.spring.securityJwt.base.service.NullTokenStore;
import eu.fraho.spring.securityJwt.base.service.RefreshTokenStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

    @Test
    public void testSaveToken() {
        Assertions.assertThrows(FeatureNotConfiguredException.class, () -> getNewInstance().saveToken(getJwtUser(), "bar"));
    }

    @Test
    public void testUseToken() {
        Assertions.assertThrows(FeatureNotConfiguredException.class, () -> getNewInstance().useToken("bar"));
    }

    @Test
    public void testListTokens() {
        Assertions.assertThrows(FeatureNotConfiguredException.class, () -> getNewInstance().listTokens());
    }

    @Test
    public void testListTokensForUser() {
        Assertions.assertThrows(FeatureNotConfiguredException.class, () -> getNewInstance().listTokens(getJwtUser()));
    }

    @Test
    public void testRevokeToken() {
        Assertions.assertThrows(FeatureNotConfiguredException.class, () -> getNewInstance().revokeToken("baz"));
    }

    @Test
    public void testRevokeTokens() {
        Assertions.assertThrows(FeatureNotConfiguredException.class, () -> getNewInstance().revokeTokens());
    }

    @Test
    public void testRevokeTokensForUser() {
        Assertions.assertThrows(FeatureNotConfiguredException.class, () -> getNewInstance().revokeTokens(getJwtUser()));
    }

    @Test
    public void testIsRefreshTokenSupported() {
        Assertions.assertFalse(getNewInstance().isRefreshTokenSupported());
    }
}