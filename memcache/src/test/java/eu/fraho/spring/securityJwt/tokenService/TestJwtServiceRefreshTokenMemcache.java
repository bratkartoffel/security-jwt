package eu.fraho.spring.securityJwt.tokenService;

import eu.fraho.spring.securityJwt.AbstractTest;
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
        Assert.assertEquals("Wrong implementation loaded", MemcacheTokenStore.class, getTokenStoreImplementation());
    }
}
