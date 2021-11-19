/*
 * MIT Licence
 * Copyright (c) 2021 Simon Frankenberger
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

    private CryptProperties getConfig() {
        return new CryptProperties();
    }

    private CryptPasswordEncoder getNewInstance(CryptProperties cryptProperties) {
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
        config.setRounds(100);
        config.afterPropertiesSet();
        CryptPasswordEncoder encoder = getNewInstance(config);

        String pwd = generatePassword();
        String password = encoder.encode(pwd);
        Assertions.assertTrue(Pattern.compile("^\\$5\\$rounds=1000\\$[a-zA-Z0-9]{16}\\$.+$").matcher(password).matches(), "Hash with wrong algorithm");
        Assertions.assertTrue(encoder.matches(pwd, password), "Password didn't validate");
    }

    @Test
    public void testEncodeSha512() {
        CryptProperties config = getConfig();
        config.setAlgorithm(CryptAlgorithm.SHA512);
        config.setRounds(100);
        config.afterPropertiesSet();
        CryptPasswordEncoder encoder = getNewInstance(config);

        String pwd = generatePassword();
        String password = encoder.encode(pwd);
        Assertions.assertTrue(Pattern.compile("^\\$6\\$rounds=1000\\$[a-zA-Z0-9]{16}\\$.+$").matcher(password).matches(), "Hash with wrong algorithm");
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

    private String generatePassword() {
        return "foobar_" + passwordNumber.getAndIncrement();
    }
}
