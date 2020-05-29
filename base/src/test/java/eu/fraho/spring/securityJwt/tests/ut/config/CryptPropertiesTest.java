/*
 * MIT Licence
 * Copyright (c) 2020 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.tests.ut.config;

import eu.fraho.spring.securityJwt.base.config.CryptProperties;
import eu.fraho.spring.securityJwt.base.dto.CryptAlgorithm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CryptPropertiesTest {
    private CryptProperties getNewInstance() {
        return new CryptProperties();
    }

    @Test
    public void testDefaultConfig() {
        getNewInstance().afterPropertiesSet();
    }

    @Test
    public void testRoundsBounds() {
        CryptProperties conf = getNewInstance();

        conf.setRounds(1);
        conf.afterPropertiesSet();
        Assertions.assertEquals(10_000, conf.getRounds(), "Rounds did not reset to default");

        conf.setRounds(1_000_000);
        conf.afterPropertiesSet();
        Assertions.assertEquals(10_000, conf.getRounds(), "Rounds did not reset to default");
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testRoundsIgnore() {
        CryptProperties conf = getNewInstance();
        conf.setAlgorithm(CryptAlgorithm.MD5);

        conf.setRounds(1);
        conf.afterPropertiesSet();
        Assertions.assertEquals(1, conf.getRounds(), "Rounds did reset to default");
    }
}
