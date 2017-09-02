/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import eu.fraho.spring.securityJwt.config.JwtRefreshConfiguration;
import eu.fraho.spring.securityJwt.config.JwtTokenConfiguration;
import eu.fraho.spring.securityJwt.dto.AccessToken;
import eu.fraho.spring.securityJwt.dto.JwtUser;
import eu.fraho.spring.securityJwt.dto.RefreshToken;
import eu.fraho.spring.securityJwt.exceptions.FeatureNotConfiguredException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JwtTokenServiceImpl implements JwtTokenService, InitializingBean {
    private final SecureRandom random = new SecureRandom();

    @NonNull
    private final JwtTokenConfiguration tokenConfig;

    @NonNull
    private final JwtRefreshConfiguration refreshConfig;

    @SuppressWarnings("SpringAutowiredFieldsWarningInspection") // not possible otherwise as this bean is lazy
    @Autowired
    @Lazy
    @Setter
    private RefreshTokenStore refreshTokenStore;

    private String truncateDeviceId(String str) {
        return Optional.ofNullable(str)
                .map(String::trim)
                .filter(e -> !e.isEmpty())
                .map(e -> e.substring(0, Math.min(e.length(), refreshConfig.getDeviceIdLength())))
                .orElse(refreshConfig.getDefaultDeviceId());
    }

    @Override
    public Integer getExpiration() {
        return tokenConfig.getExpiration().toSeconds();
    }

    @Override
    public void afterPropertiesSet() {
        if (tokenConfig.getSigner() == null) {
            log.warn("No private key specified. This service may neither issue new tokens nor use refresh tokens.");
        }
    }

    @Override
    public Optional<JwtUser> parseUser(String token) {
        try {
            JWTClaimsSet claims = SignedJWT.parse(token).getJWTClaimsSet();
            if (validateToken(token)) {
                return Optional.of(JwtUser.fromClaims(claims));
            } else {
                return Optional.empty();
            }
        } catch (ParseException e) {
            log.debug(e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    @NotNull
    public AccessToken generateToken(JwtUser user) throws JOSEException {
        if (tokenConfig.getSigner() == null) {
            throw new FeatureNotConfiguredException("Access token signing is not enabled.");
        }

        Date now = new Date();
        JWTClaimsSet claims = user.toClaims()
                .jwtID(UUID.randomUUID().toString())
                .issuer(tokenConfig.getIssuer())
                .issueTime(now)
                .notBeforeTime(now)
                .expirationTime(generateExpirationDate())
                .build();

        SignedJWT token = new SignedJWT(
                new JWSHeader(tokenConfig.getJwsAlgorithm()),
                claims);

        token.sign(tokenConfig.getSigner());
        return new AccessToken(token.serialize(), getExpiration());
    }

    @NotNull
    private Date generateExpirationDate() {
        return Date.from(ZonedDateTime.now().plusSeconds(tokenConfig.getExpiration().toSeconds()).toInstant());
    }

    private boolean validateClaims(JWTClaimsSet claims) {
        boolean result;

        Date now = new Date();
        Date exp = claims.getExpirationTime();
        Date nbf = claims.getNotBeforeTime();
        Date iat = claims.getIssueTime();

        if (exp == null) {
            exp = new Date(0);
        }
        if (nbf == null) {
            nbf = new Date(0);
        }
        if (iat == null) {
            iat = new Date(0);
        }
        log.debug("Validating claims, now={}, exp={}, nbf={}, iat={}", now, exp, nbf, iat);

        result = iat.before(now);
        log.debug("After iat < now: {}", result);
        result &= nbf.before(now);
        log.debug("After nbf < now: {}", result);
        result &= exp.after(now);
        log.debug("After exp > now: {}", result);
        return result;
    }

    @Override
    public boolean validateToken(String token) {
        log.trace("Validating {}", token);

        boolean result;
        try {
            SignedJWT parsedToken = SignedJWT.parse(token);
            JWTClaimsSet claims = parsedToken.getJWTClaimsSet();
            result = parsedToken.verify(tokenConfig.getVerifier());
            log.debug("After verify: {}", result);
            result &= validateClaims(claims);
            log.debug("After validateClaims: {}", result);
        } catch (ParseException | JOSEException e) {
            log.debug(e.getMessage(), e);
            result = false;
        }

        log.debug("Validate token returns {}", result);
        return result;
    }

    @Override
    public boolean validateToken(AccessToken token) {
        return validateToken(token.getToken());
    }

    @Override
    public Optional<String> getToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Authorization")).map(e -> e.startsWith("Bearer ") ? e.substring("Bearer".length() + 1) : e);
    }

    @Override
    @NotNull
    public RefreshToken generateRefreshToken(String user) {
        return generateRefreshToken(user, refreshConfig.getDefaultDeviceId());
    }

    @Override
    @NotNull
    public RefreshToken generateRefreshToken(String user, String deviceId) {
        final String devId = truncateDeviceId(deviceId);
        byte[] data = new byte[refreshConfig.getLength()];
        random.nextBytes(data);
        final String token = Base64.getEncoder().encodeToString(data);

        refreshTokenStore.saveToken(user, devId, token);
        return new RefreshToken(token, refreshConfig.getExpiration().toSeconds(), devId);
    }

    @Override
    public boolean useRefreshToken(String username, String refreshToken) {
        return useRefreshToken(username, refreshConfig.getDefaultDeviceId(), refreshToken);
    }

    @Override
    public boolean isRefreshTokenSupported() {
        return refreshTokenStore.isRefreshTokenSupported();
    }

    @Override
    public boolean useRefreshToken(String username, RefreshToken token) {
        return useRefreshToken(username, token.getDeviceId(), token.getToken());
    }

    @Override
    public boolean useRefreshToken(String username, String deviceId, String refreshToken) {
        if (tokenConfig.getSigner() == null) {
            throw new FeatureNotConfiguredException("Access token signing is not enabled.");
        }

        final String devId = truncateDeviceId(deviceId);
        return refreshTokenStore.useToken(username, devId, refreshToken);
    }

    @Override
    @NotNull
    public Map<String, List<RefreshToken>> listRefreshTokens() {
        return refreshTokenStore.listTokens();
    }

    @Override
    @NotNull
    public List<RefreshToken> listRefreshTokens(String username) {
        return refreshTokenStore.listTokens(username);
    }

    @Override
    public boolean revokeRefreshToken(String username, RefreshToken token) {
        return refreshTokenStore.revokeToken(username, token);
    }

    @Override
    public boolean revokeRefreshToken(String username, String deviceId) {
        return refreshTokenStore.revokeToken(username, truncateDeviceId(deviceId));
    }

    @Override
    public int revokeRefreshTokens(String username) {
        return refreshTokenStore.revokeTokens(username);
    }

    @Override
    public int clearTokens() {
        return refreshTokenStore.revokeTokens();
    }
}
