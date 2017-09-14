/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.ut.config;

import eu.fraho.spring.securityJwt.config.JwtRefreshConfiguration;
import eu.fraho.spring.securityJwt.service.NullTokenStore;
import org.junit.Assert;
import org.junit.Test;

public class JwtRefreshConfigurationTest {
    private JwtRefreshConfiguration getNewInstance() {
        return new JwtRefreshConfiguration();
    }

    @Test
    public void testLengthBounds() {
        JwtRefreshConfiguration conf = getNewInstance();

        conf.setLength(1);
        conf.afterPropertiesSet();
        Assert.assertEquals("Length did not reset to default", 24, conf.getLength());

        conf.setLength(64);
        conf.afterPropertiesSet();
        Assert.assertEquals("Length did not reset to default", 24, conf.getLength());
    }

    @Test
    public void testNullImpl() {
        JwtRefreshConfiguration conf = getNewInstance();
        conf.setCacheImpl(null);
        conf.afterPropertiesSet();
        Assert.assertEquals("CacheImapl may not be null", NullTokenStore.class, conf.getCacheImpl());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSamePaths() {
        JwtRefreshConfiguration conf = getNewInstance();
        conf.setPath("/foobar");
        conf.getCookie().setPath("/foobar");
        try {
            conf.afterPropertiesSet();
        } catch (IllegalArgumentException iae) {
            Assert.assertEquals("Wrong message text",
                    "The paths for regular refresh and cookie refresh have to be different", iae.getMessage());
            throw iae;
        }
    }

    @Test
    public void testEmptyPath() {
        JwtRefreshConfiguration conf = getNewInstance();
        conf.setPath("");
        try {
            conf.afterPropertiesSet();
        } catch (IllegalArgumentException iae) {
            Assert.assertEquals("Wrong message text",
                    "The path for refresh cookies may not be empty", iae.getMessage());
        }

        conf.setPath(null);
        try {
            conf.afterPropertiesSet();
        } catch (IllegalArgumentException iae) {
            Assert.assertEquals("Wrong message text",
                    "The path for refresh cookies may not be empty", iae.getMessage());
        }
    }

    @Test
    public void testEmptyNames() {
        JwtRefreshConfiguration conf = getNewInstance();
        conf.getCookie().setNames(new String[0]);
        try {
            conf.afterPropertiesSet();
        } catch (IllegalArgumentException iae) {
            Assert.assertEquals("Wrong message text",
                    "The path for refresh cookies may not be empty", iae.getMessage());
        }

        conf.setPath(null);
        try {
            conf.afterPropertiesSet();
        } catch (IllegalArgumentException iae) {
            Assert.assertEquals("Wrong message text",
                    "The path for refresh cookies may not be empty", iae.getMessage());
        }
    }
}
