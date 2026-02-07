/*
 * MIT Licence
 * Copyright (c) 2026 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.dataRedis.starter;

import eu.fraho.spring.securityJwt.base.config.RefreshProperties;
import eu.fraho.spring.securityJwt.base.service.RefreshTokenStore;
import eu.fraho.spring.securityJwt.base.starter.SecurityJwtBaseAutoConfiguration;
import eu.fraho.spring.securityJwt.base.starter.SecurityJwtNoRefreshStoreAutoConfiguration;
import eu.fraho.spring.securityJwt.dataRedis.config.DataRedisProperties;
import eu.fraho.spring.securityJwt.dataRedis.service.DataRedisTokenStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
@AutoConfigureAfter(SecurityJwtBaseAutoConfiguration.class)
@AutoConfigureBefore(SecurityJwtNoRefreshStoreAutoConfiguration.class)
@Slf4j
public class SecurityJwtDataRedisAutoConfiguration {
    @Bean
    public DataRedisProperties lettuceProperties() {
        log.debug("Register RedisProperties");
        return new DataRedisProperties();
    }

    @Bean
    @ConditionalOnSingleCandidate(RedisConnectionFactory.class)
    public StringRedisTemplate jwtStringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.debug("Register StringRedisTemplate");
        return new StringRedisTemplate(redisConnectionFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public RefreshTokenStore refreshTokenStore(final RefreshProperties refreshProperties,
                                               final DataRedisProperties dataRedisProperties,
                                               final UserDetailsService userDetailsService,
                                               final StringRedisTemplate jwtStringRedisTemplate) {
        log.debug("Register RedisTokenStore");
        DataRedisTokenStore store = new DataRedisTokenStore();
        store.setRefreshProperties(refreshProperties);
        store.setDataRedisProperties(dataRedisProperties);
        store.setUserDetailsService(userDetailsService);
        store.setStringRedisTemplate(jwtStringRedisTemplate);
        return store;
    }
}
