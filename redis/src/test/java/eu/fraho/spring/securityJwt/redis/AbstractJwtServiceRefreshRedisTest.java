/*
 * MIT Licence
 * Copyright (c) 2020 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.redis;

import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import eu.fraho.spring.securityJwt.base.dto.RefreshToken;
import eu.fraho.spring.securityJwt.base.service.JwtTokenService;
import eu.fraho.spring.securityJwt.base.service.RefreshTokenStore;
import eu.fraho.spring.securityJwt.base.ut.service.AbstractJwtTokenServiceWithRefreshTest;
import eu.fraho.spring.securityJwt.redis.config.RedisProperties;
import eu.fraho.spring.securityJwt.redis.service.RedisTokenStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ExtendWith(SpringExtension.class)
public class AbstractJwtServiceRefreshRedisTest extends AbstractJwtTokenServiceWithRefreshTest {
    private final RedisTokenStore refreshTokenStore;

    public AbstractJwtServiceRefreshRedisTest() throws Exception {
        refreshTokenStore = new RedisTokenStore();
        refreshTokenStore.setRefreshProperties(getRefreshProperties());
        refreshTokenStore.setRedisProperties(getRedisProperties());
        refreshTokenStore.setUserDetailsService(getUserdetailsService());
        refreshTokenStore.afterPropertiesSet();
    }

    protected RedisProperties getRedisProperties() {
        RedisProperties configuration = new RedisProperties();
        configuration.setHost(System.getProperty("fraho.jwt.refresh.redis.host", "127.0.0.1"));
        configuration.afterPropertiesSet();
        return configuration;
    }

    @Override
    protected RefreshTokenStore getRefreshStore() {
        return refreshTokenStore;
    }

    @Test
    public void testListRefreshTokensOtherEntries() throws Exception {
        JwtTokenService service = getService();

        JwtUser jsmith = getJwtUser();
        jsmith.setUsername("jsmith");
        JwtUser xsmith = getJwtUser();
        xsmith.setUsername("xsmith");

        RefreshToken tokenA = Assertions.assertTimeout(Duration.ofSeconds(1), () -> service.generateRefreshToken(jsmith));
        RefreshToken tokenB = Assertions.assertTimeout(Duration.ofSeconds(1), () -> service.generateRefreshToken(jsmith));
        RefreshToken tokenC = Assertions.assertTimeout(Duration.ofSeconds(1), () -> service.generateRefreshToken(xsmith));

        try (Jedis client = getJedisClient()) {
            Assertions.assertNull(client.set("foobar", "hi", new SetParams().xx().ex(3)));
        }

        final Map<Long, List<RefreshToken>> tokenMap = service.listRefreshTokens();
        Assertions.assertEquals(2, tokenMap.size(), "User count don't match");

        final List<RefreshToken> allTokens = tokenMap.values().stream().flatMap(Collection::stream)
                .collect(Collectors.toList());
        Assertions.assertEquals(3, allTokens.size(), "AbstractToken count don't match");
        Assertions.assertTrue(allTokens.containsAll(Arrays.asList(tokenA, tokenB, tokenC)), "Not all tokens returned");
    }

    private Jedis getJedisClient() throws Exception {
        Field memcachedClient = RedisTokenStore.class.getDeclaredField("redisPool");
        memcachedClient.setAccessible(true);
        return ((JedisPool) memcachedClient.get(refreshTokenStore)).getResource();
    }
}
