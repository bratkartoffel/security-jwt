/*
 * MIT Licence
 * Copyright (c) 2025 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.it.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"eu.fraho.spring.securityJwt"})
@Slf4j
public class TestApiApplication {
    public static void main(String[] args) {
        log.info("Starting spring context");
        SpringApplication.run(TestApiApplication.class, args);
    }
}
