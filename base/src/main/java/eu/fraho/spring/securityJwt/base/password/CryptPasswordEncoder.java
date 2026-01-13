/*
 * MIT Licence
 * Copyright (c) 2026 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.password;

import eu.fraho.spring.securityJwt.base.config.CryptProperties;
import eu.fraho.spring.securityJwt.base.dto.CryptAlgorithm;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.Crypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Encoder;

@Component
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class CryptPasswordEncoder implements PasswordEncoder {
    private final SecureRandom random = new SecureRandom();
    private final Encoder encoder = Base64.getUrlEncoder();
    private CryptProperties cryptProperties;

    private static boolean slowEquals(CharSequence a, CharSequence b) {
        int diff = a.length() ^ b.length();
        for (int i = 0; i < a.length() && i < b.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public String encode(CharSequence rawPassword) {
        final String cryptParam;
        CryptAlgorithm algorithm = cryptProperties.getAlgorithm();
        if (CryptAlgorithm.BLOWFISH.equals(algorithm)) {
            cryptParam = BCrypt.gensalt(cryptProperties.getCost(), random);
            log.trace("Encoding password with param={}", cryptParam);
            return BCrypt.hashpw(rawPassword.toString(), cryptParam);
        } else if (algorithm.isRoundsSupported()) {
            cryptParam = String.format("%srounds=%d$%s$", algorithm.getPrefix(), cryptProperties.getRounds(), generateSalt(algorithm));
        } else if (CryptAlgorithm.DES.equals(algorithm)) {
            cryptParam = generateSalt(algorithm);
        } else {
            cryptParam = String.format("%s%s$", algorithm.getPrefix(), generateSalt(algorithm));
        }
        log.trace("Encoding password with param={}", cryptParam);
        return Crypt.crypt(rawPassword.toString(), cryptParam);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        if (encodedPassword.startsWith(CryptAlgorithm.BLOWFISH.getPrefix())) {
            return BCrypt.checkpw(rawPassword.toString(), encodedPassword);
        } else {
            return slowEquals(encodedPassword, Crypt.crypt(rawPassword.toString(), encodedPassword));
        }
    }

    protected String generateSalt(CryptAlgorithm algorithm) {
        final byte[] bytes = new byte[algorithm.getSaltLength() * 2];
        random.nextBytes(bytes);
        String salt = encoder.encodeToString(bytes);
        salt = salt.replaceAll("[-_]", "");
        String realSalt = salt.substring(0, algorithm.getSaltLength());
        log.trace("Generated salt with length={}", realSalt.length());
        return realSalt;
    }

    @Autowired
    public void setCryptProperties(@NonNull CryptProperties cryptProperties) {
        this.cryptProperties = cryptProperties;
    }
}
