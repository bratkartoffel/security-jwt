/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.ut.config;

import eu.fraho.spring.securityJwt.config.JwtTokenHeaderConfiguration;
import org.junit.Assert;
import org.junit.Test;

public class TestJwtTokenHeaderConfiguration {
    private JwtTokenHeaderConfiguration getNewInstance() {
        return new JwtTokenHeaderConfiguration();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyNames() {
        JwtTokenHeaderConfiguration conf = getNewInstance();
        conf.setNames(new String[0]);
        conf.setEnabled(true);
        try {
            conf.afterPropertiesSet();
        } catch (IllegalArgumentException iae) {
            Assert.assertEquals("Wrong message text",
                    "You have to specify at least one cookie name to enable this feature", iae.getMessage());
            throw iae;
        }
    }

    @Test
    public void testDefaultConfig() {
        getNewInstance().afterPropertiesSet();
    }
}
