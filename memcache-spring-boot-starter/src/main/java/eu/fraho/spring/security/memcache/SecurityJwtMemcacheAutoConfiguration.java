/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.security.memcache;

import eu.fraho.spring.security.base.config.JwtRefreshConfiguration;
import eu.fraho.spring.security.memcache.config.MemcacheConfiguration;
import eu.fraho.spring.security.base.service.RefreshTokenStore;
import eu.fraho.spring.security_jwt.starter.SecurityJwtBaseAutoConfiguration;
import eu.fraho.spring.security_jwt.starter.SecurityJwtNoRefreshStoreAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@AutoConfigureAfter(SecurityJwtBaseAutoConfiguration.class)
@AutoConfigureBefore(SecurityJwtNoRefreshStoreAutoConfiguration.class)
@Slf4j
public class SecurityJwtMemcacheAutoConfiguration {
    @Bean
    @ConditionalOnBean(RefreshTokenStore.class)
    public MemcacheConfiguration memcacheConfiguration() {
        log.debug("Register MemcacheConfiguration");
        return new MemcacheConfiguration();
    }

    @Bean
    @ConditionalOnMissingBean
    public RefreshTokenStore refreshTokenStore(final JwtRefreshConfiguration jwtRefreshConfiguration,
                                               final MemcacheConfiguration memcacheConfiguration,
                                               final UserDetailsService userDetailsService) {
        log.debug("Register MemcacheTokenStore");
        return new MemcacheTokenStore(jwtRefreshConfiguration, memcacheConfiguration, userDetailsService);
    }
}
