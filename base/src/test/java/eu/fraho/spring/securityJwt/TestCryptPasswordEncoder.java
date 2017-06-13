/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt;

import eu.fraho.spring.securityJwt.dto.CryptAlgorithm;
import eu.fraho.spring.securityJwt.spring.TestApiApplication;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

@Getter
@Slf4j
@SpringBootTest(classes = TestApiApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TestCryptPasswordEncoder extends AbstractTest {
    private final AtomicInteger passwordNumber = new AtomicInteger();

    @BeforeClass
    public static void setup() throws IOException {
        beforeHmacClass();
    }

    @Test
    public void testEncodeDes() {
        checkAlgorithm(CryptAlgorithm.DES, Pattern.compile("^[a-zA-Z0-9]{2}[a-zA-Z0-9./]{11}$"));
    }

    @Test
    public void testEncodeMd5() {
        checkAlgorithm(CryptAlgorithm.MD5, Pattern.compile("^\\$1\\$[a-zA-Z0-9]{8}\\$.+$"));
    }

    @Test
    public void testEncodeSha256() {
        checkAlgorithm(CryptAlgorithm.SHA256, Pattern.compile("^\\$5\\$rounds=1000\\$[a-zA-Z0-9]{16}\\$.+$"));
    }

    @Test
    public void testEncodeSha512() {
        checkAlgorithm(CryptAlgorithm.SHA512, Pattern.compile("^\\$6\\$rounds=1000\\$[a-zA-Z0-9]{16}\\$.+$"));
    }

    @Test
    public void testEncodeFewRounds() {
        withTempCryptServiceField("rounds", 1, () ->
                checkAlgorithm(CryptAlgorithm.SHA256, Pattern.compile("^\\$5\\$rounds=10000\\$[a-zA-Z0-9]{16}\\$.+$")));
    }

    @Test
    public void testEncodeLargeRounds() {
        withTempCryptServiceField("rounds", 100_000_000, () ->
                checkAlgorithm(CryptAlgorithm.SHA256, Pattern.compile("^\\$5\\$rounds=10000\\$[a-zA-Z0-9]{16}\\$.+$")));
    }

    private String generatePassword() {
        return "foobar_" + passwordNumber.getAndIncrement();
    }

    private void checkAlgorithm(CryptAlgorithm algorithm, Pattern hashMatcher) {
        withTempCryptServiceField("algorithm", algorithm, () -> {
            cryptPasswordEncoder.afterPropertiesSet();
            String pwd = generatePassword();
            String password = cryptPasswordEncoder.encode(pwd);
            log.debug("Created hash: {}", password);
            Assert.assertTrue("Hash with wrong algorithm", hashMatcher.matcher(password).matches());
            Assert.assertTrue("Password didn't validate", cryptPasswordEncoder.matches(pwd, password));
        });
    }
}
