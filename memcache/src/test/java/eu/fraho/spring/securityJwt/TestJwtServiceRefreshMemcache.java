/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt;

import eu.fraho.spring.securityJwt.dto.RefreshToken;
import eu.fraho.spring.securityJwt.dto.TimeWithPeriod;
import eu.fraho.spring.securityJwt.service.MemcacheTokenStore;
import eu.fraho.spring.securityJwt.service.RefreshTokenStore;
import eu.fraho.spring.securityJwt.spring.TestApiApplication;
import eu.fraho.spring.securityJwt.tokenService.AbstractRefreshTokenTest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.spy.memcached.MemcachedClient;
import org.junit.Assert;
import org.junit.BeforeClass;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
@Setter(AccessLevel.NONE)
@Slf4j
@SpringBootTest(properties = "spring.config.location=classpath:memcache-test-refresh.yaml",
        classes = TestApiApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TestJwtServiceRefreshMemcache extends AbstractRefreshTokenTest {
    @Autowired
    private RefreshTokenStore refreshTokenStore;

    @BeforeClass
    public static void beforeClass() throws IOException {
        AbstractTest.beforeHmacClass();
    }

    @Test
    @Override
    public void checkCorrectImplementationInUse() {
        Assert.assertEquals("Wrong implementation loaded", MemcacheTokenStore.class, refreshTokenStore.getClass());
    }

    @Test(timeout = 10_000L)
    public void testListRefreshTokensOtherEntries() throws InterruptedException, ExecutionException {
        String jsmith = "jsmith";
        String xsmith = "xsmith";

        RefreshToken tokenA = jwtTokenService.generateRefreshToken(jsmith);
        RefreshToken tokenB = jwtTokenService.generateRefreshToken(jsmith, "foobar");
        RefreshToken tokenC = jwtTokenService.generateRefreshToken(xsmith);

        MemcachedClient client = ((MemcacheTokenStore) refreshTokenStore).getMemcachedClient();
        client.set("foobar", 30, "hi").get();

        final Map<String, List<RefreshToken>> tokenMap = jwtTokenService.listRefreshTokens();
        Assert.assertEquals("User count don't match", 2, tokenMap.size());

        final List<RefreshToken> allTokens = tokenMap.values().stream().flatMap(Collection::stream)
                .collect(Collectors.toList());
        Assert.assertEquals("Token count don't match", 3, allTokens.size());
        Assert.assertTrue("Not all tokens returned", allTokens.containsAll(Arrays.asList(tokenA, tokenB, tokenC)));
    }

    @Test(expected = IllegalStateException.class)
    public void testExpirationBounds() throws Exception {
        Field expiration = refreshTokenStore.getClass().getDeclaredField("refreshExpiration");
        expiration.setAccessible(true);
        Object oldValue = expiration.get(refreshTokenStore);
        try {
            expiration.set(refreshTokenStore, new TimeWithPeriod(31, TimeUnit.DAYS));
            refreshTokenStore.afterPropertiesSet();
        } finally {
            expiration.set(refreshTokenStore, oldValue);
        }
    }
}
