/*
 * MIT Licence
 * Copyright (c) 2025 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.tests.ut.service;

import eu.fraho.spring.securityJwt.base.config.TotpProperties;
import eu.fraho.spring.securityJwt.base.service.TotpService;
import eu.fraho.spring.securityJwt.base.service.TotpServiceImpl;
import eu.fraho.spring.securityJwt.base.util.TotpUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TotpServiceTest {
    private TotpProperties getConfig() {
        return new TotpProperties();
    }

    private TotpService getNewInstance(TotpProperties totpProperties) {
        TotpServiceImpl totpService = new TotpServiceImpl();
        totpService.setTotpProperties(totpProperties);
        totpService.afterPropertiesSet();
        return totpService;
    }

    @Test
    public void testGenerateSecret() {
        TotpProperties config = getConfig();
        TotpService service = getNewInstance(config);

        String secret = service.generateSecret();
        Assertions.assertNotNull(secret, "No secret generated");
        Assertions.assertNotEquals(0, secret.length(), "No secret generated");
    }

    @Test
    public void testVerify() {
        TotpProperties config = getConfig();
        TotpService service = getNewInstance(config);
        String secret = service.generateSecret();

        int lastCode = TotpUtil.getCodeForTesting(secret, -1);
        int curCode = TotpUtil.getCodeForTesting(secret, 0);
        int nextCode = TotpUtil.getCodeForTesting(secret, 1);
        int invalidCode = TotpUtil.getCodeForTesting(secret, 4);

        Assertions.assertTrue(service.verifyCode(secret, lastCode), "Last code was invalid");
        Assertions.assertTrue(service.verifyCode(secret, curCode), "Current code was invalid");
        Assertions.assertTrue(service.verifyCode(secret, nextCode), "Next code was invalid");
        Assertions.assertFalse(service.verifyCode(secret, invalidCode), "Code out of variance was valid");
    }

    @Test
    public void testVerifyShortSecret() {
        TotpProperties config = getConfig();
        TotpService service = getNewInstance(config);
        String secret = "x";

        Assertions.assertFalse(service.verifyCode(secret, 0), "Code out of variance was valid");
    }
}
