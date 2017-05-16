package eu.fraho.spring.securityJwt.passwordEncoder;

import eu.fraho.spring.securityJwt.AbstractTest;
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
@SpringBootTest(properties = "spring.config.location=classpath:test-crypt-large-rounds.yaml",
        classes = TestApiApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TestCryptPasswordEncoderLargeRounds extends AbstractCryptTest {
    @BeforeClass
    public static void beforeClass() throws IOException {
        AbstractTest.beforeHmacClass();
    }

    @Test
    public void testEncode() {
        String password = cryptPasswordEncoder.encode("foobar");
        log.debug("Created hash: {}", password);
        Assert.assertTrue("Hash with wrong algorithm", password.matches("^\\$5\\$rounds=10000\\$.{16}\\$.+$"));
        Assert.assertTrue("Password didn't validate", cryptPasswordEncoder.matches("foobar", password));
    }
}
