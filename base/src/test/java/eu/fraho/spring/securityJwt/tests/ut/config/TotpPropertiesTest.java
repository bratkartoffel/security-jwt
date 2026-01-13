/*
 * MIT Licence
 * Copyright (c) 2026 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.tests.ut.config;

import eu.fraho.spring.securityJwt.base.config.TotpProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Assertions.assertEquals(3, conf.getVariance(), "Variance did not reset to default");

        conf.setVariance(100);
        conf.afterPropertiesSet();
        Assertions.assertEquals(3, conf.getVariance(), "Variance did not reset to default");
    }

    @Test
    public void testLengthBounds() {
        TotpProperties conf = getNewInstance();

        conf.setLength(1);
        conf.afterPropertiesSet();
        Assertions.assertEquals(16, conf.getLength(), "Length did not reset to default");

        conf.setLength(128);
        conf.afterPropertiesSet();
        Assertions.assertEquals(16, conf.getLength(), "Length did not reset to default");
    }
}
