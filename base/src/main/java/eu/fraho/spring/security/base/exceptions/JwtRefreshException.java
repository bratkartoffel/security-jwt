/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.security.base.exceptions;

public class JwtRefreshException extends JwtSecurityException {
    public JwtRefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}
