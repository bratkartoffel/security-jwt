/*
 * MIT Licence
 * Copyright (c) 2026 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.dataRedis.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "fraho.jwt.refresh.redis")
@Component
@Getter
@Setter
@Slf4j
public class DataRedisProperties implements InitializingBean {
    /**
     * Defines a common prefix for all saved refresh entries.
     */
    private String prefix = "fraho-refresh";

    @Override
    public void afterPropertiesSet() {
        // nothing to check here
    }
}
