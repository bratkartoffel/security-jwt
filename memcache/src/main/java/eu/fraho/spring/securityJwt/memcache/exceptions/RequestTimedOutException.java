/*
 * MIT Licence
 * Copyright (c) 2025 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.memcache.exceptions;

public class RequestTimedOutException extends RuntimeException {
    public RequestTimedOutException(String message) {
        super(message);
    }

    public RequestTimedOutException(String message, Throwable cause) {
        super(message, cause);
    }
}
