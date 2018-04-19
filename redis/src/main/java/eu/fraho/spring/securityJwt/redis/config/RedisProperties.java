/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.redis.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPoolConfig;

@ConfigurationProperties(prefix = "fraho.jwt.refresh.redis")
@Component
@Getter
@Setter
@Slf4j
public class RedisProperties implements InitializingBean {
    /**
     * Defines a common prefix for all saved refresh entries.
     */
    private String prefix = "fraho-refresh";

    /**
     * Hostname or IP Adress of redis server
     */
    private String host = "127.0.0.1";

    /**
     * Port of redis server
     */
    private Integer port = 6379;

    /**
     * Also query for expiration when listing the tokens.
     * When this value is <code>true</code>, then an extra request is made for each token found to determine the
     * remaining time to live. You should set this parameter to <code>false</code> when running on an heavy traffic site.
     */
    private boolean fetchExpiration = true;

    @NestedConfigurationProperty
    private JedisPoolConfig poolConfig = new JedisPoolConfig();

    @Override
    public void afterPropertiesSet() {
        // nothing to check here
    }
}
