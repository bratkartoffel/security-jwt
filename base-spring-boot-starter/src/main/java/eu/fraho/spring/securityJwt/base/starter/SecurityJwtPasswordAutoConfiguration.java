/*
 * MIT Licence
 * Copyright (c) 2026 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.starter;

import eu.fraho.spring.securityJwt.base.config.CryptProperties;
import eu.fraho.spring.securityJwt.base.password.CryptPasswordEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@AutoConfigureBefore(name = "SecurityAutoConfiguration")
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
