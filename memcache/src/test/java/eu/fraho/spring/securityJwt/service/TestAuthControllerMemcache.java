/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.service;

import eu.fraho.spring.securityJwt.dto.JwtUser;
import eu.fraho.spring.securityJwt.it.AbstractTestAuthControllerWithRefresh;
import eu.fraho.spring.securityJwt.it.spring.TestApiApplication;
import eu.fraho.spring.securityJwt.it.spring.UserDetailsServiceTestImpl;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Getter
@Slf4j
@SpringBootTest(properties = "spring.config.location=classpath:memcache-test.yaml",
        classes = TestApiApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TestAuthControllerMemcache extends AbstractTestAuthControllerWithRefresh {
    public TestAuthControllerMemcache() {
        super(new UserDetailsServiceTestImpl(new StandardPasswordEncoder(), JwtUser::new));
    }
}
