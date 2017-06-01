/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.dto;

/**
 * Configuration of available crypt-algorithms.
 *
 * @see #DES
 * @see #MD5
 * @see #SHA256
 * @see #SHA512
 */
@SuppressWarnings("unused")
public enum CryptAlgorithm {
    /**
     * Use classic DES crypt (insecure, no rounds supported)
     */
    DES(true, false, "", 2),

    /**
     * Use MD5 based crypt (insecure, no rounds supported)
     */
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

    public boolean isInsecure() {
        return insecure;
    }

    public boolean isRoundsSupported() {
        return roundsSupported;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getSaltLength() {
        return saltLength;
    }
}
