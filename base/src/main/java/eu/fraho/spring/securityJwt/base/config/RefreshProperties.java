/*
 * MIT Licence
 * Copyright (c) 2025 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.config;

import eu.fraho.spring.securityJwt.base.dto.TimeWithPeriod;
import eu.fraho.spring.securityJwt.base.service.NullTokenStore;
import eu.fraho.spring.securityJwt.base.service.RefreshTokenStore;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "fraho.jwt.refresh")
@Component
@Getter
@Setter
@Slf4j
public class RefreshProperties implements InitializingBean {
    /**
     * How long are refresh tokens valid? For details please on how to specifiy this value please see the
     * documentation of the value class behind this field.
     */

    @NonNull
    private TimeWithPeriod expiration = new TimeWithPeriod("1 day");

    /**
     * Defines the length of refresh tokens in bytes, without the base64 encoding.<br>
     * Has to be between 12 and 48 (inclusive).
     */
    private int length = 24;

    /**
     * Defines the implemenation for refresh token storage. The specified class has to implement the
     * RefreshTokenStore Interface. To disable the refresh tokens just don't specify this field.
     * You have to add at least one of the optional dependencies (see README) to add refresh token support.
     * <p>
     * This field has only to be set if you are not using the provided starters.
     */

    @NonNull
    private Class<? extends RefreshTokenStore> cacheImpl = NullTokenStore.class;

    /**
     * Sets the path for the RestController, defining the endpoint for refresh requests.
     */

    @NonNull
    private String path = "/auth/refresh";

    @NestedConfigurationProperty
    private RefreshCookieProperties cookie = new RefreshCookieProperties();

    @Override
    public void afterPropertiesSet() {
        // check refresh token length
        if (length < 12 || length > 48) {
            log.warn("Refresh token length ({} <= {} <= {}), forcing to default ({})",
                    12, length, 48, 24);
            length = 24;
        }

        // cookie path may not be empty (required for controller)
        if (path.isEmpty()) {
            throw new IllegalArgumentException("The path for refresh cookies may not be empty");
        }
    }
}
