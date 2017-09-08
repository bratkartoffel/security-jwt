/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.security.internal;

import eu.fraho.spring.security.base.config.JwtRefreshConfiguration;
import eu.fraho.spring.security.base.service.RefreshTokenStore;
import eu.fraho.spring.security_jwt.starter.SecurityJwtBaseAutoConfiguration;
import eu.fraho.spring.security_jwt.starter.SecurityJwtNoRefreshStoreAutoConfiguration;
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
