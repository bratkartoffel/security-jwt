/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.service;

import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import eu.fraho.spring.securityJwt.base.dto.RefreshToken;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RefreshTokenStore extends InitializingBean {
    /**
     * Save the given token to the underlying backend.<br>
     * If the token is already registered at the store, it is overwritten and the expiration time restarts.
     *
     * @param user  The token is valid for the given user
     * @param token The token to save
     */
    void saveToken(JwtUser user, String token);

    /**
     * Try to use the given token and return the associated userdetails.
     *
     * @param token The token to use
     * @param <T>   The type of the userdetails, could be a custom implementation
     * @return An user instance if the token was valid, otherwise an empty Optional.
     */
    <T extends JwtUser> Optional<T> useToken(String token);

    /**
     * Lists all tokens for the specified user
     *
     * @param user The user to query for
     * @return A List of tokens. Maybe the tokens have not all fields set, this depends
     * on the used implementation. (e.g. memcache doesn't know the expiration)
     */

    List<RefreshToken> listTokens(JwtUser user);

    /**
     * Lists all tokens stored at the implementation.
     *
     * @return A Map with all tokens, whery the Key is the userId and the values are a list of
     * tokens for that id.
     */

    Map<Long, List<RefreshToken>> listTokens();

    /**
     * Revoke a single token.
     *
     * @param token The token to revoke
     * @return <code>true</code> if the token was found and revoked, otherweise <code>false</code>
     */
    boolean revokeToken(String token);

    /**
     * Revoke all tokens for that given user (e.g. after password change)
     *
     * @param user The user which tokens should be revoked
     * @return The count of revoked tokens.
     */
    int revokeTokens(JwtUser user);

    /**
     * Revoke all tokens stored at this implementation.
     *
     * @return The number of revoked tokens in total.
     */
    int revokeTokens();

    /**
     * Ask this service if refresh token support is enabled
     * by a third-party addon.
     *
     * @return {@code true} when refresh tokens are supported
     */
    default boolean isRefreshTokenSupported() {
        return true;
    }
}
