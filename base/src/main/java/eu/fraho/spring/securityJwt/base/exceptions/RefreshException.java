/*
 * MIT Licence
 * Copyright (c) 2025 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.exceptions;

public class RefreshException extends SecurityException {
    public RefreshException(String message) {
        super(message);
    }

    public RefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}
