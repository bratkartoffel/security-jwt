/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.spring;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;

import java.io.IOException;

@SpringBootApplication(scanBasePackages = {"eu.fraho.spring.securityJwt"})
@EnableSpringConfigured
@EntityScan(basePackages = {"eu.fraho.spring.securityJwt"})
public class TestHibernateApiApplication {
    public static void main(String[] args) throws IOException {
        TestApiApplication.main(args);
    }
}
