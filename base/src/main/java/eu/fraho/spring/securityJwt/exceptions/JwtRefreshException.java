/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.exceptions;

public class JwtRefreshException extends RuntimeException {
    public JwtRefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}
