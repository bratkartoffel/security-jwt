/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.starter.internal;

import eu.fraho.spring.securityJwt.config.JwtRefreshConfiguration;
import eu.fraho.spring.securityJwt.service.internal.InternalTokenStore;
import eu.fraho.spring.securityJwt.service.RefreshTokenStore;
import eu.fraho.spring.securityJwt.starter.SecurityJwtBaseAutoConfiguration;
import eu.fraho.spring.securityJwt.starter.SecurityJwtNoRefreshStoreAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@AutoConfigureAfter(SecurityJwtBaseAutoConfiguration.class)
@AutoConfigureBefore(SecurityJwtNoRefreshStoreAutoConfiguration.class)
@Slf4j
public class SecurityJwtInternalAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public RefreshTokenStore refreshTokenStore(final JwtRefreshConfiguration jwtRefreshConfiguration,
                                               final UserDetailsService userDetailsService) {
        log.debug("Register InternalTokenStore");
        return new InternalTokenStore(jwtRefreshConfiguration, userDetailsService);
    }
}
