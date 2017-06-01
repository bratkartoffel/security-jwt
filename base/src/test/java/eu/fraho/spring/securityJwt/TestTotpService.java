/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt;

import eu.fraho.spring.securityJwt.service.TotpServiceImpl;
import eu.fraho.spring.securityJwt.spring.TestApiApplication;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.lang.reflect.Field;

@Getter
@Setter(AccessLevel.NONE)
@Slf4j
@SpringBootTest(properties = "spring.config.location=classpath:test-totp.yaml",
        classes = TestApiApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TestTotpService extends AbstractTest {
    @Autowired
    protected TotpServiceImpl totpService = null;

    @BeforeClass
    public static void beforeClass() throws IOException {
        AbstractTest.beforeHmacClass();
    }

    @Test
    public void testCreateSecret() {
        String secret = totpService.generateSecret();
        Assert.assertNotNull("No secret generated", secret);
        Assert.assertNotEquals("No secret generated", 0, secret.length());
    }

    @Test
    public void testVerifyCode() {
        String secret = totpService.generateSecret();
        Assert.assertFalse("Empty token used", totpService.verifyCode(secret, 0));
    }

    @Test
    public void testVarianceLowerBounds() throws Exception {
        Field totpVariance = totpService.getClass().getDeclaredField("totpVariance");
        totpVariance.setAccessible(true);
        Object oldValue = totpVariance.get(totpService);
        try {
            totpVariance.set(totpService, 0);
            totpService.afterPropertiesSet();
            Assert.assertEquals("Value should be default", 3, totpVariance.get(totpService));
        } finally {
            totpVariance.set(totpService, oldValue);
        }
    }

    @Test
    public void testVarianceUpperBounds() throws Exception {
        Field totpVariance = totpService.getClass().getDeclaredField("totpVariance");
        totpVariance.setAccessible(true);
        Object oldValue = totpVariance.get(totpService);
        try {
            totpVariance.set(totpService, 11);
            totpService.afterPropertiesSet();
            Assert.assertEquals("Value should be default", 3, totpVariance.get(totpService));
        } finally {
            totpVariance.set(totpService, oldValue);
        }
    }

    @Test
    public void testLengthLowerBounds() throws Exception {
        Field totpLength = totpService.getClass().getDeclaredField("totpLength");
        totpLength.setAccessible(true);
        Object oldValue = totpLength.get(totpService);
        try {
            totpLength.set(totpService, 7);
            totpService.afterPropertiesSet();
            Assert.assertEquals("Value should be default", 16, totpLength.get(totpService));
        } finally {
            totpLength.set(totpService, oldValue);
        }
    }

    @Test
    public void testLengthUpperBounds() throws Exception {
        Field totpLength = totpService.getClass().getDeclaredField("totpLength");
        totpLength.setAccessible(true);
        Object oldValue = totpLength.get(totpService);
        try {
            totpLength.set(totpService, 33);
            totpService.afterPropertiesSet();
            Assert.assertEquals("Value should be default", 16, totpLength.get(totpService));
        } finally {
            totpLength.set(totpService, oldValue);
        }
    }
}
