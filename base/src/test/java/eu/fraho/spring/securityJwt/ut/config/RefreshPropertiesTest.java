/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.ut.config;

import eu.fraho.spring.securityJwt.config.RefreshProperties;
import eu.fraho.spring.securityJwt.service.NullTokenStore;
import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertEquals("Length did not reset to default", 24, conf.getLength());

        conf.setLength(64);
        conf.afterPropertiesSet();
        Assert.assertEquals("Length did not reset to default", 24, conf.getLength());
    }

    @Test
    public void testNullImpl() {
        RefreshProperties conf = getNewInstance();
        conf.setCacheImpl(null);
        conf.afterPropertiesSet();
        Assert.assertEquals("CacheImapl may not be null", NullTokenStore.class, conf.getCacheImpl());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyPath() {
        RefreshProperties conf = getNewInstance();
        conf.setPath("");
        try {
            conf.afterPropertiesSet();
        } catch (IllegalArgumentException iae) {
            Assert.assertEquals("Wrong message text",
                    "The path for refresh cookies may not be empty", iae.getMessage());
            throw iae;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPath() {
        RefreshProperties conf = getNewInstance();
        conf.setPath(null);
        try {
            conf.afterPropertiesSet();
        } catch (IllegalArgumentException iae) {
            Assert.assertEquals("Wrong message text",
                    "The path for refresh cookies may not be empty", iae.getMessage());
            throw iae;
        }
    }
}
