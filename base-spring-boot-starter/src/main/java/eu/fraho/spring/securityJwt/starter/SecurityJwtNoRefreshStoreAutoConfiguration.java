/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.starter;

import eu.fraho.spring.securityJwt.config.RefreshProperties;
import eu.fraho.spring.securityJwt.config.TokenProperties;
import eu.fraho.spring.securityJwt.controller.RefreshRestController;
import eu.fraho.spring.securityJwt.service.JwtTokenService;
import eu.fraho.spring.securityJwt.service.NullTokenStore;
import eu.fraho.spring.securityJwt.service.RefreshTokenStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

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
                                                       final TokenProperties tokenProperties,
                                                       final RefreshProperties refreshProperties) {
        log.debug("Register RefreshRestController");
        RefreshRestController controller = new RefreshRestController();
        controller.setJwtTokenService(jwtTokenService);
        controller.setTokenProperties(tokenProperties);
        controller.setRefreshProperties(refreshProperties);
        return controller;
    }
}
