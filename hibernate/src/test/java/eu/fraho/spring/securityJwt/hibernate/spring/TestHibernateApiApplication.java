/*
 * MIT Licence
 * Copyright (c) 2021 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.hibernate.spring;

import eu.fraho.spring.securityJwt.base.it.spring.TestApiApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;

@SpringBootApplication(scanBasePackages = {"eu.fraho.spring.securityJwt"})
@EnableSpringConfigured
@EntityScan(basePackages = {"eu.fraho.spring.securityJwt.hibernate"})
public class TestHibernateApiApplication {
    public static void main(String[] args) {
        TestApiApplication.main(args);
    }
}
