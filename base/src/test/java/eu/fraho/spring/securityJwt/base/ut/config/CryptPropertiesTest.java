/*
 * MIT Licence
 * Copyright (c) 2020 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.ut.config;

import eu.fraho.spring.securityJwt.base.config.CryptProperties;
import eu.fraho.spring.securityJwt.base.dto.CryptAlgorithm;
import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertEquals("Rounds did not reset to default", 10_000, conf.getRounds());

        conf.setRounds(1_000_000);
        conf.afterPropertiesSet();
        Assert.assertEquals("Rounds did not reset to default", 10_000, conf.getRounds());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testRoundsIgnore() {
        CryptProperties conf = getNewInstance();
        conf.setAlgorithm(CryptAlgorithm.MD5);

        conf.setRounds(1);
        conf.afterPropertiesSet();
        Assert.assertEquals("Rounds did reset to default", 1, conf.getRounds());
    }
}
