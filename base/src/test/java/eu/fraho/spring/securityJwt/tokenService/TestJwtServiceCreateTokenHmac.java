/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.tokenService;

import eu.fraho.spring.securityJwt.AbstractTest;
import eu.fraho.spring.securityJwt.spring.TestApiApplication;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Getter
@Setter(AccessLevel.NONE)
@Slf4j
@SpringBootTest(properties = "spring.config.location=classpath:test-hmac.yaml",
        classes = TestApiApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TestJwtServiceCreateTokenHmac extends AbstractCreateTokenTest {
    public static final String STATIC_SECRET = "foobarFOOBARfoobarFOOBARfoobar12";

    @BeforeClass
    public static void beforeClass() throws IOException {
        AbstractTest.beforeHmacClass();
    }

    @Test
    public void testValidateFixHmac() throws Exception {
        final String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4IiwibmJmIjoxNDk1MTE4NzE4LCJleHAiOjI0OTUxMTg3MjAsImlhdCI6MTQ5NTExODcxOH0.ARMN2DMX2mybWsvYRxyX_CAZBHU0nhGJ3qGFbn6uj10";
        Files.write(Paths.get(OUT_KEY), STATIC_SECRET.getBytes(StandardCharsets.UTF_8));
        jwtTokenService.afterPropertiesSet();
        Assert.assertTrue("Changed hmac key and validate hard coded token", jwtTokenService.validateToken(token));
    }

    @Test
    public void testValidateCheckNbf() throws Exception {
        final String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4IiwibmJmIjoyNDk1MTE4NzE4LCJleHAiOjI0OTUxMTg3MjAsImlhdCI6MTQ5NTExODcxOH0.h9jUPjrZ8vubfLqQrUYtzjjY-Zmf4kYEE6Jq7mq2T10";
        Files.write(Paths.get(OUT_KEY), STATIC_SECRET.getBytes(StandardCharsets.UTF_8));
        jwtTokenService.afterPropertiesSet();
        Assert.assertFalse("Accepted nbf value in the future", jwtTokenService.validateToken(token));
    }

    @Test
    public void testValidateCheckIat() throws Exception {
        final String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4IiwibmJmIjoxNDk1MTE4NzE4LCJleHAiOjI0OTUxMTg3MjAsImlhdCI6MjQ5NTExODcxOH0.FjQKAuwUqJfDpl4rO4fL-zKZ5P9BvjI7jI_q5EOyYBY";
        Files.write(Paths.get(OUT_KEY), STATIC_SECRET.getBytes(StandardCharsets.UTF_8));
        jwtTokenService.afterPropertiesSet();
        Assert.assertFalse("Accepted iat value in the future", jwtTokenService.validateToken(token));
    }

    @Test
    public void testValidateEmptyIat() throws Exception {
        final String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4IiwibmJmIjoxNDk1MTE4NzE4LCJleHAiOjI0OTUxMTg3MjB9.SIxUTmTLA0fW6nTh11GiLKhxZ8eikJjk-HeChogJnYc";
        Files.write(Paths.get(OUT_KEY), STATIC_SECRET.getBytes(StandardCharsets.UTF_8));
        jwtTokenService.afterPropertiesSet();
        Assert.assertTrue("Declined empty iat", jwtTokenService.validateToken(token));
    }

    @Test
    public void testValidateEmptyExp() throws Exception {
        final String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4IiwibmJmIjoxNDk1MTE4NzE4LCJpYXQiOjE0OTUxMTg3MTh9.gLl5Yss7YP9vVVwPf_XI9CDTlza4VdVdvbI5cuE-sgM";
        Files.write(Paths.get(OUT_KEY), STATIC_SECRET.getBytes(StandardCharsets.UTF_8));
        jwtTokenService.afterPropertiesSet();
        Assert.assertFalse("Accepted empty exp", jwtTokenService.validateToken(token));
    }

    @Test
    public void testValidateEmptyNbf() throws Exception {
        final String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4IiwiZXhwIjoyNDk1MTE4NzIwLCJpYXQiOjE0OTUxMTg3MTh9.Su-LzJZMPgV2n_OlZnpFjrji95CNMzfvCiVOaRPXyJg";
        Files.write(Paths.get(OUT_KEY), STATIC_SECRET.getBytes(StandardCharsets.UTF_8));
        jwtTokenService.afterPropertiesSet();
        Assert.assertTrue("Declined empty nbf", jwtTokenService.validateToken(token));
    }
}
