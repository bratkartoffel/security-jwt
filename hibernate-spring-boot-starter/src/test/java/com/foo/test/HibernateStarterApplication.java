/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package com.foo.test;

import eu.fraho.spring.securityJwt.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
@Slf4j
public class HibernateStarterApplication {
    public static void main(String[] args) throws IOException {
        AbstractTest.beforeHmacClass();
        SpringApplication.run(HibernateStarterApplication.class, args);
    }
}
