/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.service.internal;

import eu.fraho.spring.securityJwt.service.RefreshTokenStore;
import eu.fraho.spring.securityJwt.ut.service.AbstractTestJwtTokenServiceWithRefresh;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class TestJwtServiceRefreshInternal extends AbstractTestJwtTokenServiceWithRefresh {
    private RefreshTokenStore refreshTokenStore;

    public TestJwtServiceRefreshInternal() throws Exception {
        refreshTokenStore = new InternalTokenStore(getRefreshConfig(), getUserdetailsService());
        refreshTokenStore.afterPropertiesSet();
    }

    @Override
    @NotNull
    protected RefreshTokenStore getRefreshStore() {
        return refreshTokenStore;
    }
}
