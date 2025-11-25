/*
 * MIT Licence
 * Copyright (c) 2025 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.hibernate.starter;

import eu.fraho.spring.securityJwt.base.config.RefreshProperties;
import eu.fraho.spring.securityJwt.base.service.RefreshTokenStore;
import eu.fraho.spring.securityJwt.base.starter.SecurityJwtBaseAutoConfiguration;
import eu.fraho.spring.securityJwt.base.starter.SecurityJwtNoRefreshStoreAutoConfiguration;
import eu.fraho.spring.securityJwt.hibernate.dto.RefreshTokenEntity;
import eu.fraho.spring.securityJwt.hibernate.service.HibernateTokenStore;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
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
@AutoConfigurationPackage(basePackageClasses = RefreshTokenEntity.class)
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
