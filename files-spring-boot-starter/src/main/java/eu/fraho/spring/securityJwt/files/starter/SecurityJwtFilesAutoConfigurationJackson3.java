/*
 * MIT Licence
 * Copyright (c) 2026 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.files.starter;

import eu.fraho.spring.securityJwt.base.config.RefreshProperties;
import eu.fraho.spring.securityJwt.base.service.RefreshTokenStore;
import eu.fraho.spring.securityJwt.base.starter.SecurityJwtNoRefreshStoreAutoConfiguration;
import eu.fraho.spring.securityJwt.files.config.FilesProperties;
import eu.fraho.spring.securityJwt.files.service.FilesTokenStoreJackson3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Configuration
@AutoConfigureBefore(SecurityJwtNoRefreshStoreAutoConfiguration.class)
@ConditionalOnClass(ObjectMapper.class)
@Slf4j
public class SecurityJwtFilesAutoConfigurationJackson3 {
    @Bean
    @ConditionalOnMissingBean
    public RefreshTokenStore refreshTokenStore(final RefreshProperties refreshProperties,
                                               final UserDetailsService userDetailsService,
                                               final ObjectMapper objectMapper,
                                               final FilesProperties filesProperties) throws IOException {
        log.debug("Register FilesTokenStoreJackson3");
        FilesTokenStoreJackson3 store = new FilesTokenStoreJackson3();
        store.setRefreshProperties(refreshProperties);
        store.setUserDetailsService(userDetailsService);
        store.setObjectMapper(objectMapper);
        store.setFilesProperties(filesProperties);
        store.afterPropertiesSet();
        return store;
    }
}
