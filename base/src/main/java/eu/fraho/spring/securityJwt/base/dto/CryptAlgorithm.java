/*
 * MIT Licence
 * Copyright (c) 2020 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.dto;

import lombok.Getter;

/**
 * Configuration of available crypt-algorithms.
 *
 * @see #DES
 * @see #MD5
 * @see #SHA256
 * @see #SHA512
 */
@Getter
public enum CryptAlgorithm {
    /**
     * Use classic DES crypt (insecure, no rounds supported)
     *
     * @deprecated Do not use!
     */
    @Deprecated
    DES(true, false, "", 2),

    /**
     * Use MD5 based crypt (insecure, no rounds supported)
     *
     * @deprecated Do not use!
     */
    @Deprecated
    MD5(true, false, "$1$", 8),

    /**
     * Use SHA2-256 based crypt (rounds supported)
     */
    SHA256(false, true, "$5$", 16),

    /**
     * Use SHA2-512 based crypt (rounds supported)
     */
    SHA512(false, true, "$6$", 16);

    final boolean insecure;
    final boolean roundsSupported;
    final String prefix;
    final int saltLength;

    CryptAlgorithm(boolean insecure, boolean roundsSupported, String prefix, int saltLength) {
        this.insecure = insecure;
        this.roundsSupported = roundsSupported;
        this.prefix = prefix;
        this.saltLength = saltLength;
    }
}
