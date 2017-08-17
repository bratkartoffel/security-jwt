/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "fraho.jwt.refresh.memcache")
@Component
@Data
@Slf4j
public class MemcacheConfiguration implements InitializingBean {
    private String prefix = "fraho-refresh";
    private String host = "127.0.0.1";
    private Integer port = 11211;
    private Integer timeout = 5;

    @Override
    public void afterPropertiesSet() {
    }
}
