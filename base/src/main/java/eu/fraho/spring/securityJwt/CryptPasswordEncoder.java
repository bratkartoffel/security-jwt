/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt;

import eu.fraho.spring.securityJwt.dto.CryptAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.Crypt;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Random;

@Component
@Slf4j
public class CryptPasswordEncoder implements PasswordEncoder, InitializingBean {
    public static final String ALGORITHM_DEFAULT = "SHA512";

    public static final int ROUNDS_MIN = 100;
    public static final int ROUNDS_DEFAULT = 10_000;
    public static final int ROUNDS_MAX = 500_000;

    private final Random random = new SecureRandom();
    private final Encoder encoder = Base64.getUrlEncoder();
    @Value("${fraho.crypt.rounds:" + ROUNDS_DEFAULT + "}")
    private int rounds = ROUNDS_DEFAULT;
    @Value("${fraho.crypt.algorithm:" + ALGORITHM_DEFAULT + "}")
    private CryptAlgorithm algorithm = CryptAlgorithm.valueOf(ALGORITHM_DEFAULT);

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
        if (algorithm.isRoundsSupported()) {
            cryptParam = String.format("%srounds=%d$%s$", algorithm.getPrefix(), rounds, generateSalt());
        } else if (CryptAlgorithm.DES.equals(algorithm)) {
            cryptParam = generateSalt();
        } else {
            cryptParam = String.format("%s%s$", algorithm.getPrefix(), generateSalt());
        }

        log.debug("Using crypt params: {}", cryptParam);
        return Crypt.crypt(rawPassword.toString(), cryptParam);
    }

    @Override
    public boolean matches(@NotNull CharSequence rawPassword, @NotNull String encodedPassword) {
        return slowEquals(encodedPassword, Crypt.crypt(rawPassword.toString(), encodedPassword));
    }

    @NotNull
    protected String generateSalt() {
        final byte[] bytes = new byte[algorithm.getSaltLength() * 2];
        random.nextBytes(bytes);
        String salt = encoder.encodeToString(bytes);
        salt = salt.replaceAll("[-_]", "");
        return salt.substring(0, algorithm.getSaltLength());
    }

    @Override
    public void afterPropertiesSet() {
        if (algorithm.isRoundsSupported()) {
            if (rounds < ROUNDS_MIN || rounds > ROUNDS_MAX) {
                log.warn("Encryption rounds out of bounds ({} <= {} <= {}), forcing to default ({})",
                        ROUNDS_MIN, rounds, ROUNDS_MAX, ROUNDS_DEFAULT);
                rounds = ROUNDS_DEFAULT;
            }
        }

        if (algorithm.isInsecure()) {
            log.warn("Using insecure crypt variant {}. Consider upgrading to a stronger one.", algorithm);
        }
    }
}
