/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.config;

import eu.fraho.spring.securityJwt.dto.TimeWithPeriod;
import eu.fraho.spring.securityJwt.service.NullTokenStore;
import eu.fraho.spring.securityJwt.service.RefreshTokenStore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "fraho.jwt.refresh")
@Component
@Data
@Slf4j
public class JwtRefreshConfiguration implements InitializingBean {
    /**
     * How long are refresh tokens valid? For details please on how to specifiy this value please see the
     * documentation of the value class behind this field.
     */
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
    private Class<? extends RefreshTokenStore> cacheImpl = NullTokenStore.class;

    /**
     * Sets the path for the RestController, defining the endpoint for refresh requests.
     */
    private String path = "/auth/refresh";

    @Override
    public void afterPropertiesSet() {
        // check refresh token length
        if (length < 12 || length > 48) {
            log.warn("Refresh token length ({} <= {} <= {}), forcing to default ({})",
                    12, length, 48, 24);
            length = 24;
        }
        if (cacheImpl == null) {
            cacheImpl = NullTokenStore.class;
        }
    }
}
