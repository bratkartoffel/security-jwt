/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.security.base.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import eu.fraho.spring.security.base.config.JwtTokenConfiguration;
import eu.fraho.spring.security.base.config.JwtRefreshConfiguration;
import eu.fraho.spring.security.base.dto.AccessToken;
import eu.fraho.spring.security.base.dto.JwtUser;
import eu.fraho.spring.security.base.dto.RefreshToken;
import eu.fraho.spring.security.base.exceptions.FeatureNotConfiguredException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectFactory;
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

    @NonNull
    private ObjectFactory<JwtUser> jwtUser;

    @SuppressWarnings("SpringAutowiredFieldsWarningInspection") // not possible otherwise as this bean is lazy
    @Autowired
    @Lazy
    @Setter
    private RefreshTokenStore refreshTokenStore;

    @Override
    public void afterPropertiesSet() {
        if (tokenConfig.getSigner() == null) {
            log.warn("No private key specified. This service may neither issue new tokens nor use refresh tokens.");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends JwtUser> Optional<T> parseUser(@NotNull String token) {
        try {
            JWTClaimsSet claims = SignedJWT.parse(token).getJWTClaimsSet();
            if (validateToken(token)) {
                T user = (T) jwtUser.getObject();
                user.applyClaims(claims);
                return Optional.of(user);
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
    public <T extends JwtUser> AccessToken generateToken(@NotNull T user) throws JOSEException {
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
        return new AccessToken(token.serialize(), tokenConfig.getExpiration().toSeconds());
    }

    @NotNull
    private Date generateExpirationDate() {
        return Date.from(ZonedDateTime.now().plusSeconds(tokenConfig.getExpiration().toSeconds()).toInstant());
    }

    private boolean validateClaims(@NotNull JWTClaimsSet claims) {
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
    public boolean validateToken(@NotNull String token) {
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
    public boolean validateToken(@NotNull AccessToken token) {
        return validateToken(token.getToken());
    }

    @Override
    public Optional<String> getToken(@NotNull HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Authorization")).map(e -> e.startsWith("Bearer ") ? e.substring("Bearer".length() + 1) : e);
    }

    @Override
    @NotNull
    public RefreshToken generateRefreshToken(JwtUser user) {
        byte[] data = new byte[refreshConfig.getLength()];
        random.nextBytes(data);
        final String token = Base64.getEncoder().encodeToString(data);

        refreshTokenStore.saveToken(user, token);
        return new RefreshToken(token, refreshConfig.getExpiration().toSeconds());
    }

    @Override
    public boolean isRefreshTokenSupported() {
        return refreshTokenStore.isRefreshTokenSupported();
    }

    @Override
    public <T extends JwtUser> Optional<T> useRefreshToken(@NotNull RefreshToken token) {
        return useRefreshToken(token.getToken());
    }

    @Override
    public <T extends JwtUser> Optional<T> useRefreshToken(@NotNull String token) {
        if (tokenConfig.getSigner() == null) {
            throw new FeatureNotConfiguredException("Access token signing is not enabled.");
        }

        return refreshTokenStore.useToken(token);
    }

    @Override
    @NotNull
    public Map<Long, List<RefreshToken>> listRefreshTokens() {
        return refreshTokenStore.listTokens();
    }

    @Override
    @NotNull
    public List<RefreshToken> listRefreshTokens(@NotNull JwtUser user) {
        return refreshTokenStore.listTokens(user);
    }

    @Override
    public boolean revokeRefreshToken(@NotNull RefreshToken token) {
        return refreshTokenStore.revokeToken(token.getToken());
    }

    @Override
    public boolean revokeRefreshToken(@NotNull String token) {
        return refreshTokenStore.revokeToken(token);
    }

    @Override
    public int revokeRefreshTokens(@NotNull JwtUser user) {
        return refreshTokenStore.revokeTokens(user);
    }

    @Override
    public int clearTokens() {
        return refreshTokenStore.revokeTokens();
    }
}
