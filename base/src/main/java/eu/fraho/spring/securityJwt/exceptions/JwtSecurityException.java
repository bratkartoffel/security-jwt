/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.exceptions;

public class JwtSecurityException extends RuntimeException {
    public JwtSecurityException(String message) {
        super(message);
    }
}
