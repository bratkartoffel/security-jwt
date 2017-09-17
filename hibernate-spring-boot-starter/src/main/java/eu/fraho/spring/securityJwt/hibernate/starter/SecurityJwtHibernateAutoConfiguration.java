/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.hibernate.starter;

import eu.fraho.spring.securityJwt.config.JwtRefreshConfiguration;
import eu.fraho.spring.securityJwt.hibernate.service.HibernateTokenStore;
import eu.fraho.spring.securityJwt.service.RefreshTokenStore;
import eu.fraho.spring.securityJwt.starter.SecurityJwtBaseAutoConfiguration;
import eu.fraho.spring.securityJwt.starter.SecurityJwtNoRefreshStoreAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@AutoConfigureAfter(SecurityJwtBaseAutoConfiguration.class)
@AutoConfigureBefore(SecurityJwtNoRefreshStoreAutoConfiguration.class)
@EntityScan(basePackages = "eu.fraho.spring.securityJwt.hibernate.dto")
@Slf4j
public class SecurityJwtHibernateAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public RefreshTokenStore refreshTokenStore(final JwtRefreshConfiguration jwtRefreshConfiguration,
                                               final UserDetailsService userDetailsService) {
        log.debug("Register HibernateTokenStore");
        return new HibernateTokenStore(jwtRefreshConfiguration, userDetailsService);
    }
}
