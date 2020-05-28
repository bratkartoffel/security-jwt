/*
 * MIT Licence
 * Copyright (c) 2020 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.starter;

import eu.fraho.spring.securityJwt.base.config.RefreshCookieProperties;
import eu.fraho.spring.securityJwt.base.config.TokenCookieProperties;
import eu.fraho.spring.securityJwt.base.controller.RefreshRestController;
import eu.fraho.spring.securityJwt.base.service.JwtTokenService;
import eu.fraho.spring.securityJwt.base.service.NullTokenStore;
import eu.fraho.spring.securityJwt.base.service.RefreshTokenStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
@AutoConfigureAfter(SecurityJwtBaseAutoConfiguration.class)
@Slf4j
public class SecurityJwtNoRefreshStoreAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public RefreshTokenStore refreshTokenStore() {
        return new NullTokenStore();
    }

    @Bean
    @Conditional(ConditionalOnRefreshEnabled.class)
    public RefreshRestController refreshRestController(final JwtTokenService jwtTokenService,
                                                       final TokenCookieProperties tokenCookieProperties,
                                                       final RefreshCookieProperties refreshCookieProperties) {
        log.debug("Register RefreshRestController");
        RefreshRestController controller = new RefreshRestController();
        controller.setJwtTokenService(jwtTokenService);
        controller.setTokenCookieProperties(tokenCookieProperties);
        controller.setRefreshCookieProperties(refreshCookieProperties);
        return controller;
    }
}
