package eu.fraho.spring.securityJwt.tokenService;

import eu.fraho.spring.securityJwt.AbstractTest;
import eu.fraho.spring.securityJwt.dto.TimeWithPeriod;
import eu.fraho.spring.securityJwt.service.MemcacheTokenStore;
import eu.fraho.spring.securityJwt.spring.TestApiApplication;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

@Getter
@Setter(AccessLevel.NONE)
@Slf4j
@SpringBootTest(properties = "spring.config.location=classpath:test-refresh-memcache.yaml",
        classes = TestApiApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TestJwtServiceRefreshTokenMemcache extends AbstractRefreshTokenTest {
    @BeforeClass
    public static void beforeClass() throws IOException {
        AbstractTest.beforeHmacClass();
    }

    @Test
    public void checkCorrectImplementationInUse() {
        Assert.assertEquals("Wrong implementation loaded", MemcacheTokenStore.class, getTokenStoreType());
    }

    @Test(expected = IllegalStateException.class)
    public void testExpirationBounds() throws Exception {
        Field expiration = getTokenStoreType().getDeclaredField("refreshExpiration");
        expiration.setAccessible(true);
        Object oldValue = expiration.get(getTokenStoreImpl());
        try {
            expiration.set(getTokenStoreImpl(), new TimeWithPeriod(31, TimeUnit.DAYS));
            getTokenStoreImpl().afterPropertiesSet();
        } finally {
            expiration.set(getTokenStoreImpl(), oldValue);
        }
    }
}
