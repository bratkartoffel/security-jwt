/*
 * MIT Licence
 * Copyright (c) 2026 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.tests.ut.password;

import eu.fraho.spring.securityJwt.base.config.CryptProperties;
import eu.fraho.spring.securityJwt.base.dto.CryptAlgorithm;
import eu.fraho.spring.securityJwt.base.password.CryptPasswordEncoder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class CryptPasswordEncoderTest {
    private final AtomicInteger passwordNumber = new AtomicInteger();

    private static CryptProperties getConfig() {
        return new CryptProperties();
    }

    private static CryptPasswordEncoder getNewInstance(CryptProperties cryptProperties) {
        CryptPasswordEncoder cryptPasswordEncoder = new CryptPasswordEncoder();
        cryptPasswordEncoder.setCryptProperties(cryptProperties);
        return cryptPasswordEncoder;
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testEncodeDes() {
        CryptProperties config = getConfig();
        config.setAlgorithm(CryptAlgorithm.DES);
        config.afterPropertiesSet();
        CryptPasswordEncoder encoder = getNewInstance(config);

        String pwd = generatePassword();
        String password = encoder.encode(pwd);
        Assertions.assertTrue(Pattern.compile("^[a-zA-Z0-9]{2}[a-zA-Z0-9./]{11}$").matcher(password).matches(), "Hash with wrong algorithm");
        Assertions.assertTrue(encoder.matches(pwd, password), "Password didn't validate");
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testEncodeMd5() {
        CryptProperties config = getConfig();
        config.setAlgorithm(CryptAlgorithm.MD5);
        config.afterPropertiesSet();
        CryptPasswordEncoder encoder = getNewInstance(config);

        String pwd = generatePassword();
        String password = encoder.encode(pwd);
        Assertions.assertTrue(Pattern.compile("^\\$1\\$[a-zA-Z0-9]{8}\\$.+$").matcher(password).matches(), "Hash with wrong algorithm");
        Assertions.assertTrue(encoder.matches(pwd, password), "Password didn't validate");
    }

    @Test
    public void testEncodeSha256() {
        CryptProperties config = getConfig();
        config.setAlgorithm(CryptAlgorithm.SHA256);
        config.setRounds(10_000);
        config.afterPropertiesSet();
        CryptPasswordEncoder encoder = getNewInstance(config);

        String pwd = generatePassword();
        String password = encoder.encode(pwd);
        Assertions.assertTrue(Pattern.compile("^\\$5\\$rounds=10000\\$[a-zA-Z0-9]{16}\\$.+$").matcher(password).matches(), "Hash with wrong algorithm");
        Assertions.assertTrue(encoder.matches(pwd, password), "Password didn't validate");
    }

    @Test
    public void testEncodeSha512() {
        CryptProperties config = getConfig();
        config.setAlgorithm(CryptAlgorithm.SHA512);
        config.setRounds(10_000);
        config.afterPropertiesSet();
        CryptPasswordEncoder encoder = getNewInstance(config);

        String pwd = generatePassword();
        String password = encoder.encode(pwd);
        Assertions.assertTrue(Pattern.compile("^\\$6\\$rounds=10000\\$[a-zA-Z0-9]{16}\\$.+$").matcher(password).matches(), "Hash with wrong algorithm");
        Assertions.assertTrue(encoder.matches(pwd, password), "Password didn't validate");
    }

    @Test
    public void testNullsOk() {
        CryptProperties config = getConfig();
        config.afterPropertiesSet();
        CryptPasswordEncoder encoder = getNewInstance(config);
        Assertions.assertFalse(encoder.matches("foo", null), "NULL passwords did match");
        Assertions.assertFalse(encoder.matches(null, null), "NULL passwords did match");
        Assertions.assertFalse(encoder.matches(null, "bar"), "NULL passwords did match");
    }

    @Test
    public void testBlowfish() {
        String pwd = generatePassword();
        CryptProperties config = getConfig();
        config.setAlgorithm(CryptAlgorithm.BLOWFISH);
        config.setCost(13);
        config.afterPropertiesSet();
        String password = getNewInstance(config).encode(pwd);
        Assertions.assertTrue(Pattern.compile("^\\$2a\\$13\\$[a-zA-Z0-9/.]{53}$").matcher(password).matches(), "Hash with wrong algorithm");
    }

    @Test
    public void testVerifyDes() {
        CryptPasswordEncoder testee = new CryptPasswordEncoder();
        String hash = "euw4A.DfkySuE";
        Assertions.assertTrue(testee.matches("foobar", hash));
    }

    @Test
    public void testVerifyMd5() {
        CryptPasswordEncoder testee = new CryptPasswordEncoder();
        String hash = "$1$4XM02.Td$6QyF5djigTn7sHSpeVJC70";
        Assertions.assertTrue(testee.matches("foobar", hash));
    }

    @Test
    public void testVerifySha256() {
        CryptPasswordEncoder testee = new CryptPasswordEncoder();
        String hash = "$5$vW4oKb20Xu0OsQ1h$xRhEr3.pysPU..qHvUIwH0QK3RLyndmjCaps2deBwwA";
        Assertions.assertTrue(testee.matches("foobar", hash));
    }

    @Test
    public void testVerifySha512() {
        CryptPasswordEncoder testee = new CryptPasswordEncoder();
        String hash = "$6$I0X0ugWXTKiCR/Hw$IMlZcf.amW6e5lPk2wQiIS3OCsOzon3p3GW1NCVFBelXKbvrmED4I7NqZ7J2fKpEtUK8OpdQbhdyW1nGOijfe/";
        Assertions.assertTrue(testee.matches("foobar", hash));
    }

    @Test
    public void testVerifyBlowfish() {
        CryptPasswordEncoder testee = new CryptPasswordEncoder();
        String hash = "$2a$12$jchmveFlNI/zfdV5LmB89eMt7C3ylGSzW10ojZs1IkPlZx2U12fgK";
        Assertions.assertTrue(testee.matches("foobar", hash));
    }

    private String generatePassword() {
        return "foobar_" + passwordNumber.getAndIncrement();
    }
}
