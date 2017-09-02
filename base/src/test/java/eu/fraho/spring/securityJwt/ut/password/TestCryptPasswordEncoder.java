/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.ut.password;

import eu.fraho.spring.securityJwt.config.CryptConfiguration;
import eu.fraho.spring.securityJwt.dto.CryptAlgorithm;
import eu.fraho.spring.securityJwt.password.CryptPasswordEncoder;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class TestCryptPasswordEncoder {
    private final AtomicInteger passwordNumber = new AtomicInteger();

    private CryptConfiguration getConfig() {
        return new CryptConfiguration();
    }

    private CryptPasswordEncoder getNewInstance(CryptConfiguration config) {
        return new CryptPasswordEncoder(config);
    }

    @Test
    public void testEncodeDes() {
        CryptConfiguration config = getConfig();
        config.setAlgorithm(CryptAlgorithm.DES);
        config.afterPropertiesSet();
        CryptPasswordEncoder encoder = getNewInstance(config);

        String pwd = generatePassword();
        String password = encoder.encode(pwd);
        Assert.assertTrue("Hash with wrong algorithm", Pattern.compile("^[a-zA-Z0-9]{2}[a-zA-Z0-9./]{11}$").matcher(password).matches());
        Assert.assertTrue("Password didn't validate", encoder.matches(pwd, password));
    }

    @Test
    public void testEncodeMd5() {
        CryptConfiguration config = getConfig();
        config.setAlgorithm(CryptAlgorithm.MD5);
        config.afterPropertiesSet();
        CryptPasswordEncoder encoder = getNewInstance(config);

        String pwd = generatePassword();
        String password = encoder.encode(pwd);
        Assert.assertTrue("Hash with wrong algorithm", Pattern.compile("^\\$1\\$[a-zA-Z0-9]{8}\\$.+$").matcher(password).matches());
        Assert.assertTrue("Password didn't validate", encoder.matches(pwd, password));
    }

    @Test
    public void testEncodeSha256() {
        CryptConfiguration config = getConfig();
        config.setAlgorithm(CryptAlgorithm.SHA256);
        config.setRounds(100);
        config.afterPropertiesSet();
        CryptPasswordEncoder encoder = getNewInstance(config);

        String pwd = generatePassword();
        String password = encoder.encode(pwd);
        Assert.assertTrue("Hash with wrong algorithm", Pattern.compile("^\\$5\\$rounds=1000\\$[a-zA-Z0-9]{16}\\$.+$").matcher(password).matches());
        Assert.assertTrue("Password didn't validate", encoder.matches(pwd, password));
    }

    @Test
    public void testEncodeSha512() {
        CryptConfiguration config = getConfig();
        config.setAlgorithm(CryptAlgorithm.SHA512);
        config.setRounds(100);
        config.afterPropertiesSet();
        CryptPasswordEncoder encoder = getNewInstance(config);

        String pwd = generatePassword();
        String password = encoder.encode(pwd);
        Assert.assertTrue("Hash with wrong algorithm", Pattern.compile("^\\$6\\$rounds=1000\\$[a-zA-Z0-9]{16}\\$.+$").matcher(password).matches());
        Assert.assertTrue("Password didn't validate", encoder.matches(pwd, password));
    }

    @Test
    public void testNullsOk() {
        CryptConfiguration config = getConfig();
        config.afterPropertiesSet();
        CryptPasswordEncoder encoder = getNewInstance(config);
        Assert.assertFalse("NULL passwords did match", encoder.matches("foo", null));
        Assert.assertFalse("NULL passwords did match", encoder.matches(null, null));
        Assert.assertFalse("NULL passwords did match", encoder.matches(null, "bar"));
    }

    private String generatePassword() {
        return "foobar_" + passwordNumber.getAndIncrement();
    }
}
