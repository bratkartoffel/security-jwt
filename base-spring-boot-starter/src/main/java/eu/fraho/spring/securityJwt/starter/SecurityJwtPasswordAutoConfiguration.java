/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.starter;

import eu.fraho.spring.securityJwt.config.CryptProperties;
import eu.fraho.spring.securityJwt.password.CryptPasswordEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@AutoConfigureBefore(SecurityAutoConfiguration.class)
@Slf4j
public class SecurityJwtPasswordAutoConfiguration {
    @Bean
    public CryptProperties cryptProperties() {
        log.debug("Register CryptProperties");
        return new CryptProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        log.debug("Register CryptPasswordEncoder");
        CryptPasswordEncoder encoder = new CryptPasswordEncoder();
        encoder.setCryptProperties(cryptProperties());
        return encoder;
    }
}
