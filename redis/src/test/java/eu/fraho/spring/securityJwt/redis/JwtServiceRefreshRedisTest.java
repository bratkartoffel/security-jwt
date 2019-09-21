/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
public class JwtServiceRefreshRedisTest extends AbstractJwtTokenServiceWithRefreshTest {
    private final RedisTokenStore refreshTokenStore;

    public JwtServiceRefreshRedisTest() throws Exception {
        refreshTokenStore = new RedisTokenStore();
        refreshTokenStore.setRefreshProperties(getRefreshProperties());
        refreshTokenStore.setRedisProperties(getRedisProperties());
        refreshTokenStore.setUserDetailsService(getUserdetailsService());
        refreshTokenStore.afterPropertiesSet();
    }

    protected RedisProperties getRedisProperties() {
        RedisProperties configuration = new RedisProperties();
        configuration.setHost(Optional.ofNullable(System.getenv("REDIS_HOST")).orElse("127.0.0.1"));
        configuration.afterPropertiesSet();
        return configuration;
    }

    @Override
    protected RefreshTokenStore getRefreshStore() {
        return refreshTokenStore;
    }

    @Test(timeout = 10_000L)
    public void testListRefreshTokensOtherEntries() throws Exception {
        JwtTokenService service = getService();

        JwtUser jsmith = getJwtUser();
        jsmith.setUsername("jsmith");
        JwtUser xsmith = getJwtUser();
        xsmith.setUsername("xsmith");

        RefreshToken tokenA = service.generateRefreshToken(jsmith);
        RefreshToken tokenB = service.generateRefreshToken(jsmith);
        RefreshToken tokenC = service.generateRefreshToken(xsmith);

        try (Jedis client = getJedisClient()) {
            Assert.assertNull(client.set("foobar", "hi", new SetParams().xx().ex(3)));
        }

        final Map<Long, List<RefreshToken>> tokenMap = service.listRefreshTokens();
        Assert.assertEquals("User count don't match", 2, tokenMap.size());

        final List<RefreshToken> allTokens = tokenMap.values().stream().flatMap(Collection::stream)
                .collect(Collectors.toList());
        Assert.assertEquals("AbstractToken count don't match", 3, allTokens.size());
        Assert.assertTrue("Not all tokens returned", allTokens.containsAll(Arrays.asList(tokenA, tokenB, tokenC)));
    }

    private Jedis getJedisClient() throws Exception {
        Field memcachedClient = RedisTokenStore.class.getDeclaredField("redisPool");
        memcachedClient.setAccessible(true);
        return ((JedisPool) memcachedClient.get(refreshTokenStore)).getResource();
    }
}
