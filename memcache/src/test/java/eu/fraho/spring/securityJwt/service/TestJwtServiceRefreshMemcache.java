/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.service;

import eu.fraho.spring.securityJwt.config.JwtRefreshConfiguration;
import eu.fraho.spring.securityJwt.config.JwtTokenConfiguration;
import eu.fraho.spring.securityJwt.config.MemcacheConfiguration;
import eu.fraho.spring.securityJwt.dto.RefreshToken;
import eu.fraho.spring.securityJwt.dto.TimeWithPeriod;
import eu.fraho.spring.securityJwt.it.spring.TestApiApplication;
import eu.fraho.spring.securityJwt.ut.service.AbstractTestJwtTokenServiceWithRefresh;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.spy.memcached.MemcachedClient;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
@Slf4j
@SpringBootTest(properties = "spring.config.location=classpath:memcache-test.yaml",
        classes = TestApiApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TestJwtServiceRefreshMemcache extends AbstractTestJwtTokenServiceWithRefresh {
    @Autowired
    private RefreshTokenStore refreshTokenStore;

    @Autowired
    private MemcacheConfiguration memcacheConfiguration;

    @Autowired
    private JwtRefreshConfiguration refreshConfiguration;

    public TestJwtServiceRefreshMemcache() throws IOException {
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
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore);

        String jsmith = "jsmith";
        String xsmith = "xsmith";

        RefreshToken tokenA = service.generateRefreshToken(jsmith);
        RefreshToken tokenB = service.generateRefreshToken(jsmith, "foobar");
        RefreshToken tokenC = service.generateRefreshToken(xsmith);

        MemcachedClient client = ((MemcacheTokenStore) refreshTokenStore).getMemcachedClient();
        client.set("foobar", 30, "hi").get();

        final Map<String, List<RefreshToken>> tokenMap = service.listRefreshTokens();
        Assert.assertEquals("User count don't match", 2, tokenMap.size());

        final List<RefreshToken> allTokens = tokenMap.values().stream().flatMap(Collection::stream)
                .collect(Collectors.toList());
        Assert.assertEquals("Token count don't match", 3, allTokens.size());
        Assert.assertTrue("Not all tokens returned", allTokens.containsAll(Arrays.asList(tokenA, tokenB, tokenC)));
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
