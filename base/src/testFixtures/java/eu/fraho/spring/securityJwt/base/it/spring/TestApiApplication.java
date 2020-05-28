/*
 * MIT Licence
 * Copyright (c) 2020 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.it.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"eu.fraho.spring.securityJwt"})
public class TestApiApplication {
    private static final Logger log = LoggerFactory.getLogger(TestApiApplication.class);

    public static void main(String[] args) {
        log.info("Starting spring context");
        SpringApplication.run(TestApiApplication.class, args);
    }
}
