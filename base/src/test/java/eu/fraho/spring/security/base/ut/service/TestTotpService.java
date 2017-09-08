/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.security.base.ut.service;

import eu.fraho.spring.security.base.config.TotpConfiguration;
import eu.fraho.spring.security.base.service.TotpService;
import eu.fraho.spring.security.base.service.TotpServiceImpl;
import org.apache.commons.codec.binary.Base32;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TestTotpService {
    public static int getCodeForTesting(TotpService service, String secret, int varianceDiff) {
        Base32 base32 = new Base32();
        byte[] decoded = base32.decode(secret);
        long timeIndex = (System.currentTimeMillis() / 1000 / 30) + varianceDiff;

        try {
            Method method = service.getClass().getDeclaredMethod("getCode", byte[].class, long.class);
            method.setAccessible(true);
            Object result = method.invoke(service, decoded, timeIndex);
            return (Integer) result;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    private TotpConfiguration getConfig() {
        return new TotpConfiguration();
    }

    private TotpService getNewInstance(TotpConfiguration config) {
        return new TotpServiceImpl(config);
    }

    @Test
    public void testGenerateSecret() {
        TotpConfiguration config = getConfig();
        TotpService service = getNewInstance(config);

        String secret = service.generateSecret();
        Assert.assertNotNull("No secret generated", secret);
        Assert.assertNotEquals("No secret generated", 0, secret.length());
    }

    @Test
    public void testVerify() {
        TotpConfiguration config = getConfig();
        TotpService service = getNewInstance(config);
        String secret = service.generateSecret();

        int lastCode = getCodeForTesting(service, secret, -1);
        int curCode = getCodeForTesting(service, secret, 0);
        int nextCode = getCodeForTesting(service, secret, 1);
        int invalidCode = getCodeForTesting(service, secret, 4);

        Assert.assertTrue("Last code was invalid", service.verifyCode(secret, lastCode));
        Assert.assertTrue("Current code was invalid", service.verifyCode(secret, curCode));
        Assert.assertTrue("Next code was invalid", service.verifyCode(secret, nextCode));
        Assert.assertFalse("Code out of variance was valid", service.verifyCode(secret, invalidCode));
    }

    @Test
    public void testVerifyShortSecret() {
        TotpConfiguration config = getConfig();
        TotpService service = getNewInstance(config);
        String secret = "x";

        Assert.assertFalse("Code out of variance was valid", service.verifyCode(secret, 0));
    }
}
