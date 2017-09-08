/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.security.memcache;

import eu.fraho.spring.security.base.config.JwtRefreshConfiguration;
import eu.fraho.spring.security.base.config.JwtTokenConfiguration;
import eu.fraho.spring.security.base.dto.JwtUser;
import eu.fraho.spring.security.base.dto.RefreshToken;
import eu.fraho.spring.security.base.dto.TimeWithPeriod;
import eu.fraho.spring.security.memcache.config.MemcacheConfiguration;
import eu.fraho.spring.security.base.service.JwtTokenService;
import eu.fraho.spring.security.base.service.RefreshTokenStore;
import eu.fraho.spring.security.base.ut.service.AbstractTestJwtTokenServiceWithRefresh;
import lombok.extern.slf4j.Slf4j;
import net.spy.memcached.MemcachedClient;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
public class TestJwtServiceRefreshMemcache extends AbstractTestJwtTokenServiceWithRefresh {
    private JwtRefreshConfiguration refreshConfiguration;
    private RefreshTokenStore refreshTokenStore;

    public TestJwtServiceRefreshMemcache() throws Exception {
        refreshConfiguration = getRefreshConfig();
        refreshTokenStore = new MemcacheTokenStore(refreshConfiguration, new MemcacheConfiguration(), getUserdetailsService());
        refreshTokenStore.afterPropertiesSet();
    }

    @Override
    @NotNull
    protected RefreshTokenStore getRefreshStore() {
        return refreshTokenStore;
    }

    @Test(timeout = 10_000L)
    public void testListRefreshTokensOtherEntries() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore, new JwtUser());

        JwtUser jsmith = getJwtUser();
        jsmith.setUsername("jsmith");
        JwtUser xsmith = getJwtUser();
        xsmith.setUsername("xsmith");

        RefreshToken tokenA = service.generateRefreshToken(jsmith);
        RefreshToken tokenB = service.generateRefreshToken(jsmith);
        RefreshToken tokenC = service.generateRefreshToken(xsmith);

        MemcachedClient client = getMemcachedClient();
        client.set("foobar", 30, "hi").get();

        final Map<Long, List<RefreshToken>> tokenMap = service.listRefreshTokens();
        Assert.assertEquals("User count don't match", 2, tokenMap.size());

        final List<RefreshToken> allTokens = tokenMap.values().stream().flatMap(Collection::stream)
                .collect(Collectors.toList());
        Assert.assertEquals("Token count don't match", 3, allTokens.size());
        Assert.assertTrue("Not all tokens returned", allTokens.containsAll(Arrays.asList(tokenA, tokenB, tokenC)));
    }

    private MemcachedClient getMemcachedClient() throws Exception {
        Field memcachedClient = MemcacheTokenStore.class.getDeclaredField("memcachedClient");
        memcachedClient.setAccessible(true);
        return (MemcachedClient) memcachedClient.get(refreshTokenStore);
    }

    @Test(expected = IllegalStateException.class)
    public void testExpirationBounds() throws Exception {
        Field expiration = JwtRefreshConfiguration.class.getDeclaredField("expiration");
        expiration.setAccessible(true);
        Object oldValue = expiration.get(refreshConfiguration);
        try {
            expiration.set(refreshConfiguration, new TimeWithPeriod(31, TimeUnit.DAYS));
            refreshTokenStore.afterPropertiesSet();
        } finally {
            expiration.set(refreshConfiguration, oldValue);
        }
    }
}
