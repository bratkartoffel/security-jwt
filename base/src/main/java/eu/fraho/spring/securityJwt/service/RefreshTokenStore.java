/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.service;

import eu.fraho.spring.securityJwt.dto.RefreshToken;
import eu.fraho.spring.securityJwt.dto.TimeWithPeriod;
import eu.fraho.spring.securityJwt.exceptions.JwtRefreshException;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public interface RefreshTokenStore extends InitializingBean {
    void saveToken(String username, String deviceId, String token);

    boolean useToken(String username, String deviceId, String token);

    List<RefreshToken> listTokens(String username);

    Map<String, List<RefreshToken>> listTokens();

    boolean revokeToken(String username, RefreshToken token);

    boolean revokeToken(String username, String deviceId);

    int revokeTokens(String username);

    int revokeTokens();

    TimeWithPeriod getRefreshExpiration();

    /**
     * Try to achieve some time constant compare.
     * Should be used to verify that a provided token is the same as the stored one.
     *
     * @param a First
     * @param b Second
     * @return {@code true} if both are equal
     */
    default boolean tokenEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }

    /**
     * Helper method for modules to achieve higher code coverage.
     * Execute the given action and return the result.
     * If an exception occurs, it is wrapped as an {@link JwtRefreshException}
     *
     * @param message The exception message on error
     * @param action  The action to execute
     * @param <T>     Generic return type
     * @return Result of action
     */
    default <T> T exceptionWrapper(String message, Supplier<T> action) {
        try {
            return action.get();
        } catch (Exception ex) {
            throw new JwtRefreshException(message, ex);
        }
    }

}
