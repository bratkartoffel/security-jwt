/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.starter;

import eu.fraho.spring.securityJwt.config.JwtRefreshConfiguration;
import eu.fraho.spring.securityJwt.service.InternalTokenStore;
import eu.fraho.spring.securityJwt.service.RefreshTokenStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(SecurityJwtBaseAutoConfiguration.class)
@AutoConfigureBefore(SecurityJwtNoRefreshStoreAutoConfiguration.class)
@Slf4j
public class SecurityJwtInternalAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public RefreshTokenStore refreshTokenStore(final JwtRefreshConfiguration jwtRefreshConfiguration) {
        log.debug("Register InternalTokenStore");
        return new InternalTokenStore(jwtRefreshConfiguration);
    }
}
