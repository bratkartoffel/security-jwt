/*
 * MIT Licence
 * Copyright (c) 2021 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.service;

public interface TotpService {
    /**
     * Verify the given code against the stored secret.
     *
     * @param secret The shared secret between client and server
     * @param code   The code to verify
     * @return {@code true} if the given code is within the configured variance bounds.
     * @throws NullPointerException if secret is null
     */
    boolean verifyCode(String secret, int code);

    /**
     * Generate a new shared secret.
     *
     * @return A base32-encoded secret for the client
     */
    String generateSecret();
}
