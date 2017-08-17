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
    private TimeWithPeriod expiration = new TimeWithPeriod("1 day");
    private int length = 24;
    private int deviceIdLength = 32;
    private String delimiter = ";";
    private String defaultDeviceId = "__default";
    private Class<? extends RefreshTokenStore> cacheImpl = NullTokenStore.class;

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
