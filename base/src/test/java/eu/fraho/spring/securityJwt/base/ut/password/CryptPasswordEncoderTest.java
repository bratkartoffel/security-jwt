/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.ut.password;

import eu.fraho.spring.securityJwt.base.config.CryptProperties;
import eu.fraho.spring.securityJwt.base.dto.CryptAlgorithm;
import eu.fraho.spring.securityJwt.base.password.CryptPasswordEncoder;
import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertTrue("Hash with wrong algorithm", Pattern.compile("^[a-zA-Z0-9]{2}[a-zA-Z0-9./]{11}$").matcher(password).matches());
        Assert.assertTrue("Password didn't validate", encoder.matches(pwd, password));
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
        Assert.assertTrue("Hash with wrong algorithm", Pattern.compile("^\\$1\\$[a-zA-Z0-9]{8}\\$.+$").matcher(password).matches());
        Assert.assertTrue("Password didn't validate", encoder.matches(pwd, password));
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
        Assert.assertTrue("Hash with wrong algorithm", Pattern.compile("^\\$5\\$rounds=1000\\$[a-zA-Z0-9]{16}\\$.+$").matcher(password).matches());
        Assert.assertTrue("Password didn't validate", encoder.matches(pwd, password));
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
        Assert.assertTrue("Hash with wrong algorithm", Pattern.compile("^\\$6\\$rounds=1000\\$[a-zA-Z0-9]{16}\\$.+$").matcher(password).matches());
        Assert.assertTrue("Password didn't validate", encoder.matches(pwd, password));
    }

    @Test
    public void testNullsOk() {
        CryptProperties config = getConfig();
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
