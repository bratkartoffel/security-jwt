/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.service;

import eu.fraho.spring.securityJwt.dto.JwtUser;
import eu.fraho.spring.securityJwt.it.spring.UserDetailsServiceTestImpl;
import eu.fraho.spring.securityJwt.ut.service.AbstractTestJwtTokenServiceWithRefresh;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

@Slf4j
public class TestJwtServiceRefreshInternal extends AbstractTestJwtTokenServiceWithRefresh {
    private RefreshTokenStore refreshTokenStore;

    public TestJwtServiceRefreshInternal() throws Exception {
        refreshTokenStore = new InternalTokenStore(getRefreshConfig(), new UserDetailsServiceTestImpl(new StandardPasswordEncoder(), JwtUser::new));
        refreshTokenStore.afterPropertiesSet();
    }

    @Override
    @NotNull
    protected RefreshTokenStore getRefreshStore() {
        return refreshTokenStore;
    }
}
