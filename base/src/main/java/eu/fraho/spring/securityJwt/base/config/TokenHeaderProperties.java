/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.config;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "fraho.jwt.token.header")
@Component
@Getter
@Setter
@Slf4j
public class TokenHeaderProperties implements InitializingBean {
    /**
     * Enables support for tokens sent as a header.
     */
    private boolean enabled = true;

    /**
     * Sets the name of the headers which may contain the token.
     */
    @NotNull
    @NonNull
    private String[] names = new String[]{"Authorization"};

    @Override
    public void afterPropertiesSet() {
        if (enabled) {
            if (names.length == 0) {
                throw new IllegalArgumentException("You have to specify at least one cookie name to enable this feature");
            }

            log.info("Enabling authorization support via headers");
        }
    }
}
