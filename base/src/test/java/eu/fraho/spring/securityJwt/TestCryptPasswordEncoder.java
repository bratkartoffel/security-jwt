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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Getter
@Slf4j
@SpringBootTest(classes = TestApiApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TestCryptPasswordEncoder extends AbstractTest {
    @Test
    public void testEncodeDes() {
        withTempCryptServiceField("algorithm", CryptAlgorithm.DES, () -> {
            cryptPasswordEncoder.afterPropertiesSet();
            String password = cryptPasswordEncoder.encode("foobar");
            log.debug("Created hash: {}", password);
            Assert.assertFalse("Hash with wrong algorithm", password.startsWith("$"));
            Assert.assertTrue("Password didn't validate", cryptPasswordEncoder.matches("foobar", password));
        });
    }

    @Test
    public void testEncodeMd5() {
        withTempCryptServiceField("algorithm", CryptAlgorithm.MD5, () -> {
            cryptPasswordEncoder.afterPropertiesSet();
            String password = cryptPasswordEncoder.encode("foobar");
            log.debug("Created hash: {}", password);
            Assert.assertTrue("Hash with wrong algorithm", password.matches("^\\$1\\$.{8}\\$.+$"));
            Assert.assertTrue("Password didn't validate", cryptPasswordEncoder.matches("foobar", password));
        });
    }

    @Test
    public void testEncodeSha256() {
        withTempCryptServiceField("algorithm", CryptAlgorithm.SHA256, () -> {
            cryptPasswordEncoder.afterPropertiesSet();
            String password = cryptPasswordEncoder.encode("foobar");
            log.debug("Created hash: {}", password);
            Assert.assertTrue("Hash with wrong algorithm", password.matches("^\\$5\\$rounds=1000\\$.{16}\\$.+$"));
            Assert.assertTrue("Password didn't validate", cryptPasswordEncoder.matches("foobar", password));
        });
    }

    @Test
    public void testEncodeSha512() {
        withTempCryptServiceField("algorithm", CryptAlgorithm.SHA512, () -> {
            cryptPasswordEncoder.afterPropertiesSet();
            String password = cryptPasswordEncoder.encode("foobar");
            log.debug("Created hash: {}", password);
            Assert.assertTrue("Hash with wrong algorithm", password.matches("^\\$6\\$rounds=1000\\$.{16}\\$.+$"));
            Assert.assertTrue("Password didn't validate", cryptPasswordEncoder.matches("foobar", password));
        });
    }

    @Test
    public void testEncodeFewRounds() {
        withTempCryptServiceField("rounds", 1, () -> {
            cryptPasswordEncoder.afterPropertiesSet();
            String password = cryptPasswordEncoder.encode("foobar");
            log.debug("Created hash: {}", password);
            Assert.assertTrue("Hash with wrong algorithm", password.matches("^\\$5\\$rounds=10000\\$.{16}\\$.+$"));
            Assert.assertTrue("Password didn't validate", cryptPasswordEncoder.matches("foobar", password));
        });
    }

    @Test
    public void testEncodeLargeRounds() {
        withTempCryptServiceField("rounds", 100_000_000, () -> {
            cryptPasswordEncoder.afterPropertiesSet();
            String password = cryptPasswordEncoder.encode("foobar");
            log.debug("Created hash: {}", password);
            Assert.assertTrue("Hash with wrong algorithm", password.matches("^\\$5\\$rounds=10000\\$.{16}\\$.+$"));
            Assert.assertTrue("Password didn't validate", cryptPasswordEncoder.matches("foobar", password));
        });
    }

}
