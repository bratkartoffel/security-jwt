/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.ut.config;

import eu.fraho.spring.securityJwt.config.TotpProperties;
import org.junit.Assert;
import org.junit.Test;

public class TotpPropertiesTest {
    private TotpProperties getNewInstance() {
        return new TotpProperties();
    }

    @Test
    public void testDefaultConfig() {
        getNewInstance().afterPropertiesSet();
    }

    @Test
    public void testVarianceBounds() {
        TotpProperties conf = getNewInstance();

        conf.setVariance(0);
        conf.afterPropertiesSet();
        Assert.assertEquals("Variance did not reset to default", 3, conf.getVariance());

        conf.setVariance(100);
        conf.afterPropertiesSet();
        Assert.assertEquals("Variance did not reset to default", 3, conf.getVariance());
    }

    @Test
    public void testLengthBounds() {
        TotpProperties conf = getNewInstance();

        conf.setLength(1);
        conf.afterPropertiesSet();
        Assert.assertEquals("Length did not reset to default", 16, conf.getLength());

        conf.setLength(128);
        conf.afterPropertiesSet();
        Assert.assertEquals("Length did not reset to default", 16, conf.getLength());
    }
}
