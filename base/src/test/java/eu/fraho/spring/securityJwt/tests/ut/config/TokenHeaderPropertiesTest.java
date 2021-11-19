/*
 * MIT Licence
 * Copyright (c) 2021 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.tests.ut.config;

import eu.fraho.spring.securityJwt.base.config.TokenHeaderProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TokenHeaderPropertiesTest {
    private TokenHeaderProperties getNewInstance() {
        return new TokenHeaderProperties();
    }

    @Test
    public void testEmptyNames() {
        TokenHeaderProperties conf = getNewInstance();
        conf.setNames(new String[0]);
        conf.setEnabled(true);
        Assertions.assertThrows(IllegalArgumentException.class, conf::afterPropertiesSet);
    }

    @Test
    public void testDefaultConfig() {
        getNewInstance().afterPropertiesSet();
    }
}
