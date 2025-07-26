/*
 * MIT Licence
 * Copyright (c) 2025 Simon Frankenberger
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
        conf.setAlgorithm(CryptAlgorithm.SHA256);

        conf.setRounds(9_999);
        conf.afterPropertiesSet();
        Assertions.assertEquals(50_000, conf.getRounds(), "Rounds did not reset to default");

        conf.setRounds(100_000_001);
        conf.afterPropertiesSet();
        Assertions.assertEquals(50_000, conf.getRounds(), "Rounds did not reset to default");
    }

    @Test
    public void testCostBounds() {
        CryptProperties conf = getNewInstance();
        conf.setAlgorithm(CryptAlgorithm.BLOWFISH);

        conf.setCost(9);
        conf.afterPropertiesSet();
        Assertions.assertEquals(12, conf.getCost(), "Cost did not reset to default");

        conf.setCost(20);
        conf.afterPropertiesSet();
        Assertions.assertEquals(12, conf.getCost(), "Rounds did not reset to default");
    }

    @Test
    public void testRoundsIgnore() {
        CryptProperties conf = getNewInstance();
        conf.setAlgorithm(CryptAlgorithm.BLOWFISH);

        conf.setRounds(1);
        conf.afterPropertiesSet();
        Assertions.assertEquals(1, conf.getRounds(), "Rounds was reset to default");
    }

    @Test
    public void testCostIgnore() {
        CryptProperties conf = getNewInstance();
        conf.setAlgorithm(CryptAlgorithm.SHA512);

        conf.setCost(1);
        conf.afterPropertiesSet();
        Assertions.assertEquals(1, conf.getCost(), "Cost was reset to default");
    }
}
