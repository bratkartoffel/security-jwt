/*
 * MIT Licence
 * Copyright (c) 2022 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.tests.ut.config;

import eu.fraho.spring.securityJwt.base.config.RefreshProperties;
import eu.fraho.spring.securityJwt.base.service.NullTokenStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RefreshPropertiesTest {
    private RefreshProperties getNewInstance() {
        return new RefreshProperties();
    }

    @Test
    public void testDefaultConfig() {
        getNewInstance().afterPropertiesSet();
    }

    @Test
    public void testLengthBounds() {
        RefreshProperties conf = getNewInstance();

        conf.setLength(1);
        conf.afterPropertiesSet();
        Assertions.assertEquals(24, conf.getLength(), "Length did not reset to default");

        conf.setLength(64);
        conf.afterPropertiesSet();
        Assertions.assertEquals(24, conf.getLength(), "Length did not reset to default");
    }

    @Test
    public void testNullImpl() {
        RefreshProperties conf = getNewInstance();
        try {
            conf.setCacheImpl(null);
            Assertions.fail("Setting null on cache impl worked");
        } catch (NullPointerException npe) {
            // just ignore, we expect that here
        }
        conf.afterPropertiesSet();
        Assertions.assertEquals(NullTokenStore.class, conf.getCacheImpl(), "CacheImapl may not be null");
    }

    @Test
    public void testEmptyPath() {
        RefreshProperties conf = getNewInstance();
        conf.setPath("");
        Assertions.assertThrows(IllegalArgumentException.class, conf::afterPropertiesSet);
    }

    @Test
    public void testNullPath() {
        RefreshProperties conf = getNewInstance();
        Assertions.assertThrows(NullPointerException.class, () -> conf.setPath(null));

        conf.afterPropertiesSet();
        Assertions.assertNotNull(conf.getPath(), "Path may not be null");
    }
}
