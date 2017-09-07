/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import eu.fraho.spring.securityJwt.dto.AccessToken;
import eu.fraho.spring.securityJwt.dto.JwtUser;
import eu.fraho.spring.securityJwt.dto.RefreshToken;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface JwtTokenService {
    /**
     * Parse a token and return the encapsulated user object.
     * See {@link JwtUser#applyClaims(JWTClaimsSet)} for a list of copied / supported attributes.
     *
     * @param token The JWT to parse
     * @param <T>   Any configured subtype of JwtUser
     * @return an user object
     */
    <T extends JwtUser> Optional<T> parseUser(@NotNull String token);

    /**
     * Generate a new token for the given user.
     * See {@link JwtUser#toClaims()} for a list of copied / supported attributes.
     *
     * @param user The user to create the token for.
     * @param <T>  Any configured subtype of JwtUser
     * @return The token (Base64 encoded)
     * @throws JOSEException            When the token could not be signed
     * @throws IllegalArgumentException If no private key is specified (service cannot sign tokens)
     */
    @NotNull <T extends JwtUser> AccessToken generateToken(@NotNull T user) throws JOSEException;

    /**
     * Parse the token, validate the signature and check notBeforeDate and expirationTime.
     *
     * @param token The token to validate
     * @return {@code true} if the token passed all checks (is valid and trusted) or otherwise {@code false}
     */
    boolean validateToken(@NotNull String token);

    /**
     * Parse the token, validate the signature and check notBeforeDate and expirationTime.
     *
     * @param token The token to validate
     * @return {@code true} if the token passed all checks (is valid and trusted) or otherwise {@code false}
     */
    boolean validateToken(@NotNull AccessToken token);

    /**
     * Helper method to extract the token from a requests headers.
     *
     * @param request the request
     * @return optional token
     */
    Optional<String> getToken(@NotNull HttpServletRequest request);

    /**
     * Gracefully ask this service if refresh token support is enabled
     * by a third-party addon.
     *
     * @return {@code true} when refresh tokens are supported
     */
    boolean isRefreshTokenSupported();

    /**
     * Generate a simple refresh token using the default device id. This token is not a "normal" JWT.
     * It's just a base64-encoded string of random data.
     * Returns {@code null} if the property {@code fraho.jwt.refresh.enabled} is set to {@code false}.
     *
     * @param user The refresh token will be issued to this user
     * @return The refresh token (Base64 encoded) or {@code null} if property {@code fraho.jwt.refresh.enabled} is set to {@code false}.
     */
    @NotNull
    RefreshToken generateRefreshToken(JwtUser user);

    /**
     * Use and invalidate a refresh token for the given user using the default device id.
     * Each refresh token may only be used once.
     * Returns always {@code false} if property {@code fraho.jwt.refresh.enabled} is set to {@code false}
     * or no private key for signing is configured.
     *
     * @param refreshToken the refresh token to use (base64 encoded)
     * @param <T>          Any configured subtype of JwtUser
     * @return {@code true} only if the refresh token is valid and not been used before
     */
    <T extends JwtUser> Optional<T> useRefreshToken(@NotNull String refreshToken);

    /**
     * Use and invalidate a refresh token for the given user.
     * Each refresh token may only be used once.
     * Returns always {@code false} if property {@code fraho.jwt.refresh.enabled} is set to {@code false}
     * or no private key for signing is configured.
     *
     * @param token the refresh token to use
     * @param <T>   Any configured subtype of JwtUser
     * @return {@code true} only if the refresh token is valid and not been used before
     */
    <T extends JwtUser> Optional<T> useRefreshToken(@NotNull RefreshToken token);

    /**
     * List all currently active refresh tokens from the backend cache.
     *
     * @return a Map with all active refresh tokens.
     * The {@code key} is the username, the {@code value} is the list of tokens for that user
     */
    @NotNull
    Map<Long, List<RefreshToken>> listRefreshTokens();

    /**
     * List all currently active refresh tokens for the given username.
     *
     * @param user the user to filter the refresh tokens
     * @return list of refresh tokens
     */
    @NotNull
    List<RefreshToken> listRefreshTokens(@NotNull JwtUser user);

    /**
     * Revoke the given token from the stored refresh token list
     *
     * @param token the token to remove
     * @return {@code true} if the token was revoked
     */
    boolean revokeRefreshToken(@NotNull RefreshToken token);

    /**
     * Revoke the given token from the stored refresh token list
     *
     * @param token The token to revoke
     * @return {@code true} if the token was revoked
     */
    boolean revokeRefreshToken(@NotNull String token);

    /**
     * Revokes all  refresh tokens from the stored refresh token list for the specified user
     *
     * @param user The user which tokens should be revoked
     * @return count of revoked tokens
     */
    int revokeRefreshTokens(@NotNull JwtUser user);

    /**
     * Revoke all tokens from the stored refresh token list
     *
     * @return count of revoked tokens
     */
    int clearTokens();
}
