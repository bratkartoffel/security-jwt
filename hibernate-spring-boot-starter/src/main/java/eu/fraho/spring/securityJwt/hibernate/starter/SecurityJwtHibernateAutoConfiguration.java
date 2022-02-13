/*
 * MIT Licence
 * Copyright (c) 2022 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.hibernate.starter;

import eu.fraho.spring.securityJwt.base.config.RefreshProperties;
import eu.fraho.spring.securityJwt.base.service.RefreshTokenStore;
import eu.fraho.spring.securityJwt.base.starter.SecurityJwtBaseAutoConfiguration;
import eu.fraho.spring.securityJwt.base.starter.SecurityJwtNoRefreshStoreAutoConfiguration;
import eu.fraho.spring.securityJwt.hibernate.service.HibernateTokenStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

import javax.persistence.EntityManager;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
@AutoConfigureAfter(SecurityJwtBaseAutoConfiguration.class)
@AutoConfigureBefore(SecurityJwtNoRefreshStoreAutoConfiguration.class)
@EntityScan(basePackages = "eu.fraho.spring.securityJwt.hibernate.dto")
@Slf4j
public class SecurityJwtHibernateAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public RefreshTokenStore refreshTokenStore(final RefreshProperties refreshProperties,
                                               final UserDetailsService userDetailsService,
                                               final EntityManager entityManager) {
        log.debug("Register HibernateTokenStore");
        HibernateTokenStore store = new HibernateTokenStore();
        store.setRefreshProperties(refreshProperties);
        store.setUserDetailsService(userDetailsService);
        store.setEntityManager(entityManager);
        return store;
    }
}
