/*
 * MIT Licence
 * Copyright (c) 2025 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.memcache;

import eu.fraho.spring.securityJwt.base.config.RefreshProperties;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import eu.fraho.spring.securityJwt.base.dto.RefreshToken;
import eu.fraho.spring.securityJwt.base.dto.TimeWithPeriod;
import eu.fraho.spring.securityJwt.base.service.JwtTokenService;
import eu.fraho.spring.securityJwt.base.service.RefreshTokenStore;
import eu.fraho.spring.securityJwt.base.ut.service.AbstractJwtTokenServiceWithRefreshTest;
import eu.fraho.spring.securityJwt.memcache.config.MemcacheProperties;
import eu.fraho.spring.securityJwt.memcache.service.MemcacheTokenStore;
import net.spy.memcached.MemcachedClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@ExtendWith(SpringExtension.class)
public class AbstractJwtServiceRefreshMemcacheTest extends AbstractJwtTokenServiceWithRefreshTest {
    private final RefreshProperties refreshProperties;
    private final MemcacheTokenStore refreshTokenStore;

    public AbstractJwtServiceRefreshMemcacheTest() throws Exception {
        refreshProperties = getRefreshProperties();
        refreshTokenStore = new MemcacheTokenStore();
        refreshTokenStore.setRefreshProperties(refreshProperties);
        refreshTokenStore.setMemcacheProperties(getMemcacheProperties());
        refreshTokenStore.setUserDetailsService(getUserdetailsService());
        refreshTokenStore.afterPropertiesSet();
    }

    protected MemcacheProperties getMemcacheProperties() {
        MemcacheProperties configuration = new MemcacheProperties();
        configuration.setHost(System.getProperty("fraho.jwt.refresh.memcache.host", "127.0.0.1"));
        configuration.afterPropertiesSet();
        return configuration;
    }

    @BeforeEach
    public void clearCache() throws Exception {
        Assertions.assertTrue(getMemcachedClient().flush().get(1, TimeUnit.SECONDS));
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

        MemcachedClient client = getMemcachedClient();
        RefreshToken tokenA = Assertions.assertTimeout(Duration.ofSeconds(1), () -> service.generateRefreshToken(jsmith));
        RefreshToken tokenB = Assertions.assertTimeout(Duration.ofSeconds(1), () -> service.generateRefreshToken(jsmith));
        RefreshToken tokenC = Assertions.assertTimeout(Duration.ofSeconds(1), () -> service.generateRefreshToken(xsmith));

        client.set("foobar", 30, "hi").get();

        final Map<Long, List<RefreshToken>> tokenMap = service.listRefreshTokens();
        Assertions.assertEquals(2, tokenMap.size(), "User count don't match");

        final List<RefreshToken> allTokens = tokenMap.values().stream().flatMap(Collection::stream)
                .collect(Collectors.toList());
        Assertions.assertEquals(3, allTokens.size(), "AbstractToken count don't match");
        Assertions.assertTrue(allTokens.containsAll(Arrays.asList(tokenA, tokenB, tokenC)), "Not all tokens returned");
    }

    private MemcachedClient getMemcachedClient() throws Exception {
        Field memcachedClient = MemcacheTokenStore.class.getDeclaredField("memcachedClient");
        memcachedClient.setAccessible(true);
        return (MemcachedClient) memcachedClient.get(refreshTokenStore);
    }

    @Test
    public void testExpirationBounds() throws Exception {
        Field expiration = RefreshProperties.class.getDeclaredField("expiration");
        expiration.setAccessible(true);
        Object oldValue = expiration.get(refreshProperties);
        try {
            expiration.set(refreshProperties, new TimeWithPeriod(31, ChronoUnit.DAYS));
            Assertions.assertThrows(IllegalStateException.class, refreshTokenStore::afterPropertiesSet);
        } finally {
            expiration.set(refreshProperties, oldValue);
        }
    }
}
