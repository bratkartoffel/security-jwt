/*
 * MIT Licence
 * Copyright (c) 2022 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.internal;

import eu.fraho.spring.securityJwt.base.service.RefreshTokenStore;
import eu.fraho.spring.securityJwt.base.ut.service.AbstractJwtTokenServiceWithRefreshTest;
import eu.fraho.spring.securityJwt.internal.service.InternalTokenStore;

public class AbstractJwtServiceRefreshInternalTest extends AbstractJwtTokenServiceWithRefreshTest {
    private final InternalTokenStore refreshTokenStore;

    public AbstractJwtServiceRefreshInternalTest() throws Exception {
        refreshTokenStore = new InternalTokenStore();
        refreshTokenStore.setRefreshProperties(getRefreshProperties());
        refreshTokenStore.setUserDetailsService(getUserdetailsService());
        refreshTokenStore.afterPropertiesSet();
    }

    @Override
    protected RefreshTokenStore getRefreshStore() {
        return refreshTokenStore;
    }
}
