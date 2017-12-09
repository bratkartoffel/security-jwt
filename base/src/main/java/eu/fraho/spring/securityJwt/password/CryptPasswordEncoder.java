/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.password;

import eu.fraho.spring.securityJwt.config.CryptProperties;
import eu.fraho.spring.securityJwt.dto.CryptAlgorithm;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.Crypt;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Random;

@Component
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class CryptPasswordEncoder implements PasswordEncoder {
    private final Random random = new SecureRandom();

    private final Encoder encoder = Base64.getUrlEncoder();

    @Setter(onMethod = @__({@Autowired, @NonNull}))
    private CryptProperties cryptProperties;

    private static boolean slowEquals(@NotNull CharSequence a, @NotNull CharSequence b) {
        int diff = a.length() ^ b.length();
        for (int i = 0; i < a.length() && i < b.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }

    @Override
    public String encode(@NotNull CharSequence rawPassword) {
        final String cryptParam;
        CryptAlgorithm algorithm = cryptProperties.getAlgorithm();
        if (algorithm.isRoundsSupported()) {
            cryptParam = String.format("%srounds=%d$%s$", algorithm.getPrefix(), cryptProperties.getRounds(), generateSalt());
        } else if (CryptAlgorithm.DES.equals(algorithm)) {
            cryptParam = generateSalt();
        } else {
            cryptParam = String.format("%s%s$", algorithm.getPrefix(), generateSalt());
        }
        log.trace("Encoding password with param={}", cryptParam);
        return Crypt.crypt(rawPassword.toString(), cryptParam);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return rawPassword != null && encodedPassword != null
                && slowEquals(encodedPassword, Crypt.crypt(rawPassword.toString(), encodedPassword));
    }

    @NotNull
    protected String generateSalt() {
        CryptAlgorithm algorithm = cryptProperties.getAlgorithm();
        final byte[] bytes = new byte[algorithm.getSaltLength() * 2];
        random.nextBytes(bytes);
        String salt = encoder.encodeToString(bytes);
        salt = salt.replaceAll("[-_]", "");
        String realSalt = salt.substring(0, algorithm.getSaltLength());
        log.trace("Generated salt for encoding, length={}", realSalt.length());
        return realSalt;
    }
}
