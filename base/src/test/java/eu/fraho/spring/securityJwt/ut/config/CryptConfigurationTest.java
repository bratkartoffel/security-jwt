/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.ut.config;

import eu.fraho.spring.securityJwt.config.CryptConfiguration;
import eu.fraho.spring.securityJwt.dto.CryptAlgorithm;
import org.junit.Assert;
import org.junit.Test;

public class CryptConfigurationTest {
    private CryptConfiguration getNewInstance() {
        return new CryptConfiguration();
    }

    @Test
    public void testRoundsBounds() {
        CryptConfiguration conf = getNewInstance();

        conf.setRounds(1);
        conf.afterPropertiesSet();
        Assert.assertEquals("Rounds did not reset to default", 10_000, conf.getRounds());

        conf.setRounds(1_000_000);
        conf.afterPropertiesSet();
        Assert.assertEquals("Rounds did not reset to default", 10_000, conf.getRounds());
    }

    @Test
    public void testRoundsIgnore() {
        CryptConfiguration conf = getNewInstance();
        conf.setAlgorithm(CryptAlgorithm.MD5);

        conf.setRounds(1);
        conf.afterPropertiesSet();
        Assert.assertEquals("Rounds did reset to default", 1, conf.getRounds());
    }
}
