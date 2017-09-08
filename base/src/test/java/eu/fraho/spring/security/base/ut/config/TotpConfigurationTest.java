/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.security.base.ut.config;

import eu.fraho.spring.security.base.config.TotpConfiguration;
import org.junit.Assert;
import org.junit.Test;

public class TotpConfigurationTest {
    private TotpConfiguration getNewInstance() {
        return new TotpConfiguration();
    }

    @Test
    public void testVarianceBounds() {
        TotpConfiguration conf = getNewInstance();

        conf.setVariance(0);
        conf.afterPropertiesSet();
        Assert.assertEquals("Variance did not reset to default", 3, conf.getVariance());

        conf.setVariance(100);
        conf.afterPropertiesSet();
        Assert.assertEquals("Variance did not reset to default", 3, conf.getVariance());
    }

    @Test
    public void testLengthBounds() {
        TotpConfiguration conf = getNewInstance();

        conf.setLength(1);
        conf.afterPropertiesSet();
        Assert.assertEquals("Length did not reset to default", 16, conf.getLength());

        conf.setLength(128);
        conf.afterPropertiesSet();
        Assert.assertEquals("Length did not reset to default", 16, conf.getLength());
    }
}
