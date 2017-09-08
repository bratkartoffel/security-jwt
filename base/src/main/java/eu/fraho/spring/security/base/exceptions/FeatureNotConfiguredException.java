/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.security.base.exceptions;

public class FeatureNotConfiguredException extends JwtSecurityException {
    public FeatureNotConfiguredException(String message) {
        super(message);
    }
}
