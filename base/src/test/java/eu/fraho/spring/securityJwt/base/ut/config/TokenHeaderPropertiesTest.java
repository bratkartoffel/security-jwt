/*
 * MIT Licence
 * Copyright (c) 2020 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.ut.config;

import eu.fraho.spring.securityJwt.base.config.TokenHeaderProperties;
import org.junit.Assert;
import org.junit.Test;

public class TokenHeaderPropertiesTest {
    private TokenHeaderProperties getNewInstance() {
        return new TokenHeaderProperties();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyNames() {
        TokenHeaderProperties conf = getNewInstance();
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
