/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.internal.service;

import eu.fraho.spring.securityJwt.service.RefreshTokenStore;
import eu.fraho.spring.securityJwt.ut.service.AbstractJwtTokenServiceWithRefreshTest;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class JwtServiceRefreshInternalTest extends AbstractJwtTokenServiceWithRefreshTest {
    private final RefreshTokenStore refreshTokenStore;

    public JwtServiceRefreshInternalTest() throws Exception {
        refreshTokenStore = new InternalTokenStore(getRefreshProperties(), getUserdetailsService());
        refreshTokenStore.afterPropertiesSet();
    }

    @Override
    @NotNull
    protected RefreshTokenStore getRefreshStore() {
        return refreshTokenStore;
    }
}
