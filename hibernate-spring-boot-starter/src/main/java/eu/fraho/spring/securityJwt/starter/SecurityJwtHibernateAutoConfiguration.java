/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.starter;

import eu.fraho.spring.securityJwt.config.JwtRefreshConfiguration;
import eu.fraho.spring.securityJwt.service.HibernateTokenStore;
import eu.fraho.spring.securityJwt.service.RefreshTokenStore;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(SecurityJwtBaseAutoConfiguration.class)
@AutoConfigureBefore(SecurityJwtNoRefreshStoreAutoConfiguration.class)
@EntityScan(basePackages = "eu.fraho.spring.securityJwt.dto")
public class SecurityJwtHibernateAutoConfiguration {
    @Bean
    public RefreshTokenStore hibernateTokenStore(final JwtRefreshConfiguration jwtRefreshConfiguration) {
        return new HibernateTokenStore(jwtRefreshConfiguration);
    }
}
