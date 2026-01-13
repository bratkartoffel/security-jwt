/*
 * MIT Licence
 * Copyright (c) 2026 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.files.starter;

import eu.fraho.spring.securityJwt.files.config.FilesProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class SecurityJwtFilesAutoConfiguration {
    @Bean
    public FilesProperties filesProperties() {
        log.debug("Register FilesProperties");
        return new FilesProperties();
    }
}
