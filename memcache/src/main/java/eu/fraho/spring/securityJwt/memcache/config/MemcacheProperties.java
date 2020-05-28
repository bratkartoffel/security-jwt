/*
 * MIT Licence
 * Copyright (c) 2020 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.memcache.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "fraho.jwt.refresh.memcache")
@Component
@Getter
@Setter
@Slf4j
public class MemcacheProperties implements InitializingBean {
    /**
     * Defines a common prefix for all saved refresh entries.
     */
    private String prefix = "fraho-refresh";

    /**
     * Hostname or IP Adress of memcache server
     */
    private String host = "127.0.0.1";

    /**
     * Port of memcache server
     */
    private Integer port = 11211;

    /**
     * Timeout (in seconds) when talking to memcache server
     */
    private Integer timeout = 5;

    @Override
    public void afterPropertiesSet() {
        // nothing to check here
    }
}
