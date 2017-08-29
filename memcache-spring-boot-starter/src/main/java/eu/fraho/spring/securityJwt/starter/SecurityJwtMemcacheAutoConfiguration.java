/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.starter;

import eu.fraho.spring.securityJwt.config.JwtRefreshConfiguration;
import eu.fraho.spring.securityJwt.config.MemcacheConfiguration;
import eu.fraho.spring.securityJwt.service.MemcacheTokenStore;
import eu.fraho.spring.securityJwt.service.RefreshTokenStore;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(SecurityJwtBaseAutoConfiguration.class)
@AutoConfigureBefore(SecurityJwtNoRefreshStoreAutoConfiguration.class)
public class SecurityJwtMemcacheAutoConfiguration {
    @Bean
    public MemcacheConfiguration memcacheConfiguration() {
        return new MemcacheConfiguration();
    }

    @Bean
    public RefreshTokenStore memcacheTokenStore(final JwtRefreshConfiguration jwtRefreshConfiguration,
                                                final MemcacheConfiguration memcacheConfiguration) {
        return new MemcacheTokenStore(jwtRefreshConfiguration, memcacheConfiguration);
    }
}
