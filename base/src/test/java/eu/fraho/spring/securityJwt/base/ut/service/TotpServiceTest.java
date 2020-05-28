/*
 * MIT Licence
 * Copyright (c) 2020 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.ut.service;

import eu.fraho.spring.securityJwt.base.config.TotpProperties;
import eu.fraho.spring.securityJwt.base.service.TotpService;
import eu.fraho.spring.securityJwt.base.service.TotpServiceImpl;
import eu.fraho.spring.securityJwt.base.util.TotpUtil;
import org.junit.Assert;
import org.junit.Test;

public class TotpServiceTest {
    private TotpProperties getConfig() {
        return new TotpProperties();
    }

    private TotpService getNewInstance(TotpProperties totpProperties) {
        TotpServiceImpl totpService = new TotpServiceImpl();
        totpService.setTotpProperties(totpProperties);
        return totpService;
    }

    @Test
    public void testGenerateSecret() {
        TotpProperties config = getConfig();
        TotpService service = getNewInstance(config);

        String secret = service.generateSecret();
        Assert.assertNotNull("No secret generated", secret);
        Assert.assertNotEquals("No secret generated", 0, secret.length());
    }

    @Test
    public void testVerify() {
        TotpProperties config = getConfig();
        TotpService service = getNewInstance(config);
        String secret = service.generateSecret();

        int lastCode = TotpUtil.getCodeForTesting(service, secret, -1);
        int curCode = TotpUtil.getCodeForTesting(service, secret, 0);
        int nextCode = TotpUtil.getCodeForTesting(service, secret, 1);
        int invalidCode = TotpUtil.getCodeForTesting(service, secret, 4);

        Assert.assertTrue("Last code was invalid", service.verifyCode(secret, lastCode));
        Assert.assertTrue("Current code was invalid", service.verifyCode(secret, curCode));
        Assert.assertTrue("Next code was invalid", service.verifyCode(secret, nextCode));
        Assert.assertFalse("Code out of variance was valid", service.verifyCode(secret, invalidCode));
    }

    @Test
    public void testVerifyShortSecret() {
        TotpProperties config = getConfig();
        TotpService service = getNewInstance(config);
        String secret = "x";

        Assert.assertFalse("Code out of variance was valid", service.verifyCode(secret, 0));
    }
}
