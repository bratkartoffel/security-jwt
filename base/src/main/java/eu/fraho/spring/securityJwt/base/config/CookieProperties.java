/*
 * MIT Licence
 * Copyright (c) 2022 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.config;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;

public interface CookieProperties extends InitializingBean {
    boolean isEnabled();

    String[] getNames();

    String getPath();

    String getDomain();

    boolean isHttpOnly();

    boolean isSecure();

    Logger getLog();

    default void afterPropertiesSet() {
        if (isEnabled() && getNames().length == 0) {
            throw new IllegalArgumentException("You have to specify at least one cookie name to enable this feature");
        }

        if (isEnabled()) {
            getLog().info("Enabling authorization support via cookies");
            if (!isHttpOnly()) {
                getLog().warn("Disabling httpOnly flag for cookies, this is not recommended");
            }
            if (!isSecure()) {
                getLog().warn("Disabling secure flag for cookies, this is not recommended");
            }
        }
    }
}
