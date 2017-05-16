package eu.fraho.spring.securityJwt;

import eu.fraho.spring.securityJwt.service.TotpService;
import eu.fraho.spring.securityJwt.spring.TestApiApplication;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@Getter
@Setter(AccessLevel.NONE)
@Slf4j
@SpringBootTest(properties = "spring.config.location=classpath:test-totp.yaml",
        classes = TestApiApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TestTotpService extends AbstractTest {
    @Autowired
    protected TotpService totpService = null;

    @BeforeClass
    public static void beforeClass() throws IOException {
        AbstractTest.beforeHmacClass();
    }

    @Test
    public void testCreateSecret() {
        String secret = totpService.generateSecret();
        Assert.assertNotNull("No secret generated", secret);
        Assert.assertNotEquals("No secret generated", 0, secret.length());
    }

    @Test
    public void testVerifyCode() {
        String secret = totpService.generateSecret();
        Assert.assertFalse("Empty token used", totpService.verifyCode(secret, 0));
    }
}
