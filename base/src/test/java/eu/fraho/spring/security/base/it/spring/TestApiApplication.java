/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.security.base.it.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;

import java.io.IOException;

@SpringBootApplication(scanBasePackages = {"eu.fraho.spring.securityJwt"})
@EnableSpringConfigured
@Slf4j
public class TestApiApplication {
    public static void main(String[] args) throws IOException {
        log.info("Starting spring context");
        SpringApplication.run(TestApiApplication.class, args);
    }
}
