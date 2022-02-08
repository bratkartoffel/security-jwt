/*
 * MIT Licence
 * Copyright (c) 2022 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.tests.ut.config;

import eu.fraho.spring.securityJwt.base.config.RefreshCookieProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RefreshCookiePropertiesTest {
    private RefreshCookieProperties getNewInstance() {
        return new RefreshCookieProperties();
    }

    @Test
    public void testDefaultConfig() {
        getNewInstance().afterPropertiesSet();
    }

    @Test
    public void testEmptyNames() {
        RefreshCookieProperties conf = getNewInstance();
        conf.setNames(new String[0]);
        conf.setEnabled(true);
        Assertions.assertThrows(IllegalArgumentException.class, conf::afterPropertiesSet);
    }

    @Test
    public void testLogging() {
        RefreshCookieProperties conf = new RefreshCookieProperties();
        conf.setEnabled(true);
        conf.setHttpOnly(false);
        conf.setSecure(false);
        conf.afterPropertiesSet();
    }
}
