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

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface JwtTokenService {
    String DEFAULT_DEVICE_ID = "__default";
    String DEFAULT_ALGORITHM = "ES256";
    String DEFAULT_ISSUER = "fraho-security";
    String DEFAULT_EXPIRATION = "1 hour";
    String DEFAULT_REFRESH_EXPIRATION = "1 day";
    int DEFAULT_MAX_DEVICE_ID_LENGTH = 32;
    String DEFAULT_CACHE_PREFIX = "fraho-refresh";
    String DEFAULT_CACHE_IMPL = "#{null}";

    int REFRESH_TOKEN_LEN_MIN = 12;
    int REFRESH_TOKEN_LEN_DEFAULT = 24;
    int REFRESH_TOKEN_LEN_MAX = 48;

    /**
     * Parse a token and return the encapsulated user object.
     * See {@link JwtUser#fromClaims(JWTClaimsSet)} for a list of copied / supported attributes.
     *
     * @param token The JWT to parse
     * @return an user object
     */
    Optional<JwtUser> parseUser(String token);

    /**
     * Generate a new token for the given user.
     * See {@link JwtUser#toClaims()} for a list of copied / supported attributes.
     *
     * @param user The user to create the token for.
     * @return The token (Base64 encoded)
     * @throws JOSEException            When the token could not be signed
     * @throws IllegalArgumentException If no private key is specified (service cannot sign tokens)
     */
    AccessToken generateToken(JwtUser user) throws JOSEException;

    /**
     * Parse the token, validate the signature and check notBeforeDate and expirationTime.
     *
     * @param token The token to validate
     * @return {@code true} if the token passed all checks (is valid and trusted) or otherwise {@code false}
     */
    boolean validateToken(String token);

    /**
     * Parse the token, validate the signature and check notBeforeDate and expirationTime.
     *
     * @param token The token to validate
     * @return {@code true} if the token passed all checks (is valid and trusted) or otherwise {@code false}
     */
    boolean validateToken(AccessToken token);

    /**
     * Helper method to extract the token from a requests headers.
     *
     * @param request the request
     * @return optional token
     */
    Optional<String> getToken(HttpServletRequest request);

    /**
     * @return The validity of newly created access tokens in seconds
     */
    Integer getExpiration();


    /**
     * @return The validity of newly created refresh tokens in seconds
     */
    Integer getRefreshExpiration();

    /**
     * Generate a simple refresh token using the default device id. This token is not a "normal" JWT.
     * It's just a base64-encoded string of random data.
     * Returns {@code null} if the property {@code fraho.jwt.refresh.enabled} is set to {@code false}.
     *
     * @param user The refresh token will be issued to this user
     * @return The refresh token (Base64 encoded) or {@code null} if property {@code fraho.jwt.refresh.enabled} is set to {@code false}.
     */
    RefreshToken generateRefreshToken(String user);

    /**
     * Generate a simple refresh token. This token is not a "normal" JWT.
     * It's just a base64-encoded string of random data.
     * Returns {@code null} if the property {@code fraho.jwt.refresh.enabled} is set to {@code false}.
     *
     * @param user     The refresh token will be issued to this user
     * @param deviceId device id for this refresh token to support multiple tokens / devices.
     *                 This id will be truncated to the configured length.
     * @return The refresh token (Base64 encoded) or {@code null} if property {@code fraho.jwt.refresh.enabled} is set to {@code false}.
     */
    RefreshToken generateRefreshToken(String user, String deviceId);

    /**
     * Use and invalidate a refresh token for the given user using the default device id.
     * Each refresh token may only be used once.
     * Returns always {@code false} if property {@code fraho.jwt.refresh.enabled} is set to {@code false}
     * or no private key for signing is configured.
     *
     * @param username     the user requesting a new access token
     * @param refreshToken the refresh token to use (base64 encoded)
     * @return {@code true} only if the refresh token is valid and not been used before
     */
    boolean useRefreshToken(String username, String refreshToken);

    /**
     * Use and invalidate a refresh token for the given user.
     * Each refresh token may only be used once.
     * Returns always {@code false} if property {@code fraho.jwt.refresh.enabled} is set to {@code false}
     * or no private key for signing is configured.
     *
     * @param username     the user requesting a new access token
     * @param deviceId     Optional device id for this refresh token to support multiple tokens / devices
     *                     This id will be truncated to the configured length.
     * @param refreshToken the refresh token to use (base64 encoded)
     * @return {@code true} only if the refresh token is valid and not been used before
     */
    boolean useRefreshToken(String username, String deviceId, String refreshToken);

    /**
     * Use and invalidate a refresh token for the given user.
     * Each refresh token may only be used once.
     * Returns always {@code false} if property {@code fraho.jwt.refresh.enabled} is set to {@code false}
     * or no private key for signing is configured.
     *
     * @param username the user requesting a new access token
     * @param token    the refresh token to use
     * @return {@code true} only if the refresh token is valid and not been used before
     */
    boolean useRefreshToken(String username, RefreshToken token);

    /**
     * List all currently active refresh tokens from the backend cache.
     *
     * @return a Map with all active refresh tokens.
     * The {@code key} is the username, the {@code value} is the list of tokens for that user
     */
    Map<String, List<RefreshToken>> listRefreshTokens();

    /**
     * List all currently active refresh tokens for the given username.
     *
     * @param username the username to filter the refresh tokens
     * @return list of refresh tokens
     */
    List<RefreshToken> listRefreshTokens(String username);

    /**
     * Revoke the given token from the stored refresh token list
     *
     * @param username username
     * @param token    the token to remove
     * @return {@code true} if the token was revoked
     */
    boolean revokeRefreshToken(String username, RefreshToken token);

    /**
     * Revoke the given token from the stored refresh token list
     *
     * @param username username
     * @param deviceId the deviceId to remove
     * @return {@code true} if the token was revoked
     */
    boolean revokeRefreshToken(String username, String deviceId);

    /**
     * Revokes all  refresh tokens from the stored refresh token list for the specified user
     *
     * @param username username
     * @return count of revoked tokens
     */
    int revokeRefreshTokens(String username);

    /**
     * Revoke all tokens from the stored refresh token list
     *
     * @return count of revoked tokens
     */
    int clearTokens();
}
