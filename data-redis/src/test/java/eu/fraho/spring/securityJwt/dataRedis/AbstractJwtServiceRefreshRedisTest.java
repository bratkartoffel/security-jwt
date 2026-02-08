/*
 * MIT Licence
 * Copyright (c) 2026 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.dataRedis;

import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import eu.fraho.spring.securityJwt.base.dto.RefreshToken;
import eu.fraho.spring.securityJwt.base.service.JwtTokenService;
import eu.fraho.spring.securityJwt.base.service.RefreshTokenStore;
import eu.fraho.spring.securityJwt.base.ut.service.AbstractJwtTokenServiceWithRefreshTest;
import eu.fraho.spring.securityJwt.dataRedis.config.DataRedisProperties;
import eu.fraho.spring.securityJwt.dataRedis.service.DataRedisTokenStore;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class AbstractJwtServiceRefreshRedisTest extends AbstractJwtTokenServiceWithRefreshTest {
    private final DataRedisTokenStore refreshTokenStore;

    public AbstractJwtServiceRefreshRedisTest() throws Exception {
        refreshTokenStore = new DataRedisTokenStore();
        refreshTokenStore.setRefreshProperties(getRefreshProperties());
        refreshTokenStore.setDataRedisProperties(getRedisProperties());
        refreshTokenStore.setUserDetailsService(getUserdetailsService());
        refreshTokenStore.setStringRedisTemplate(getStringRedisTemplate(getLettuceConnectionFactory()));
        refreshTokenStore.afterPropertiesSet();
    }

    protected DataRedisProperties getRedisProperties() {
        DataRedisProperties configuration = new DataRedisProperties();
        configuration.afterPropertiesSet();
        return configuration;
    }

    protected StringRedisTemplate getStringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory);
        stringRedisTemplate.afterPropertiesSet();
        return stringRedisTemplate;
    }

    protected RedisStandaloneConfiguration getRedisConfiguration() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(System.getProperty("spring.data.redis.host", "127.0.0.1"));
        configuration.setPassword(System.getProperty("spring.data.redis.password", "changeit"));
        return configuration;
    }

    protected RedisConnectionFactory getLettuceConnectionFactory() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(getRedisConfiguration());
        factory.afterPropertiesSet();
        return factory;
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

        Assertions.assertTrue(getStringRedisTemplate().opsForValue().setIfAbsent("foobar", "hi", Duration.ofSeconds(3)));

        final Map<Long, List<RefreshToken>> tokenMap = service.listRefreshTokens();
        Assertions.assertEquals(2, tokenMap.size(), "User count don't match");

        final List<RefreshToken> allTokens = tokenMap.values().stream().flatMap(Collection::stream).toList();
        Assertions.assertEquals(3, allTokens.size(), "AbstractToken count don't match");
        Assertions.assertTrue(allTokens.containsAll(Arrays.asList(tokenA, tokenB, tokenC)), "Not all tokens returned");
    }

    private StringRedisTemplate getStringRedisTemplate() throws Exception {
        Field memcachedClient = DataRedisTokenStore.class.getDeclaredField("redisTemplate");
        memcachedClient.setAccessible(true);
        return ((StringRedisTemplate) memcachedClient.get(refreshTokenStore));
    }
}
