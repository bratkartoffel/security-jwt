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
import eu.fraho.spring.securityJwt.config.*;
import eu.fraho.spring.securityJwt.dto.AccessToken;
import eu.fraho.spring.securityJwt.dto.JwtUser;
import eu.fraho.spring.securityJwt.dto.RefreshToken;
import eu.fraho.spring.securityJwt.exceptions.FeatureNotConfiguredException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
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
    private final JwtTokenCookieConfiguration tokenCookieConfiguration;

    @NonNull
    private final JwtTokenHeaderConfiguration tokenHeaderConfiguration;

    @NonNull
    private final JwtRefreshCookieConfiguration refreshCookieConfiguration;

    @NonNull
    private final ObjectFactory<JwtUser> jwtUser;

    // not possible otherwise, as the RegisterRefreshTokenStore comes pretty late
    @SuppressWarnings("SpringAutowiredFieldsWarningInspection")
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
        Optional<T> result = Optional.empty();
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (validateToken(signedJWT)) {
                log.debug("Successfully validated token by client");
                JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
                T user = (T) jwtUser.getObject();
                user.applyClaims(claims);
                log.debug("Token resulted in user {}", user);
                result = Optional.of(user);
            }
        } catch (ParseException e) {
            log.warn("Could not parse token", e);
        }
        return result;
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

    @Override
    public boolean validateToken(@NotNull AccessToken token) {
        return validateToken(token.getToken());
    }

    @Override
    public boolean validateToken(@NotNull String token) {
        boolean result = false;
        try {
            result = validateToken(SignedJWT.parse(token));
        } catch (ParseException e) {
            log.error("Supplied token did not validate", e);
        }
        return result;
    }

    @Override
    public boolean validateToken(@NotNull SignedJWT signedJWT) {
        boolean result;
        try {
            result = signedJWT.verify(tokenConfig.getVerifier());
            log.debug("Token signature verified, result={}", result);
            result &= areClaimsValid(signedJWT.getJWTClaimsSet());
            log.debug("Claims verified, result={}", result);
        } catch (ParseException | JOSEException e) {
            log.error("Supplied token did not validate", e);
            result = false;
        }
        return result;
    }

    private boolean areClaimsValid(@NotNull JWTClaimsSet claims) {
        Date now = new Date();
        Date exp = Optional.ofNullable(claims.getExpirationTime()).orElse(new Date(0));
        Date nbf = Optional.ofNullable(claims.getNotBeforeTime()).orElse(new Date(0));
        Date iat = Optional.ofNullable(claims.getIssueTime()).orElse(new Date(0));

        log.debug("Validating claims");
        boolean result = iat.before(now);
        log.debug("iat={} < now={}, result={}", iat, now, result);
        result &= nbf.before(now);
        log.debug("nbf={} < now={}, result={}", nbf, now, result);
        result &= exp.after(now);
        log.debug("exp={} > now={}, result={}", exp, now, result);
        return result;
    }

    @Override
    @Deprecated
    public Optional<String> getToken(@NotNull HttpServletRequest request) {
        return getAccessToken(request);
    }

    @Override
    public Optional<String> getAccessToken(@NotNull HttpServletRequest request) {
        Optional<String> result = Optional.empty();
        if (tokenHeaderConfiguration.isEnabled()) {
            log.debug("Extracting token from header");
            result = extractHeaderToken(tokenHeaderConfiguration.getNames(), request);
        }
        if (!result.isPresent() && tokenCookieConfiguration.isEnabled()) {
            log.debug("Extracting token from cookies");
            result = extractCookieToken(tokenCookieConfiguration.getNames(), request.getCookies());
        }
        return result;
    }

    @Override
    public Optional<String> getRefreshToken(@NotNull HttpServletRequest request) {
        Optional<String> result = Optional.empty();
        if (refreshCookieConfiguration.isEnabled()) {
            log.debug("Extracting refreshtoken from cookies");
            result = extractCookieToken(refreshCookieConfiguration.getNames(), request.getCookies());
        }
        return result;
    }

    private Optional<String> extractHeaderToken(@NotNull String[] names, @NotNull HttpServletRequest request) {
        return Arrays.stream(names)
                .map(request::getHeader)
                .filter(Objects::nonNull)
                .findFirst()
                .map(e -> e.startsWith("Bearer ") ? e.substring(7) : e);
    }

    private Optional<String> extractCookieToken(@NotNull String[] names, @Nullable Cookie[] cookies) {
        Optional<String> result = Optional.empty();
        if (cookies != null) {
            result = Arrays.stream(names)
                    .map(String::toLowerCase)
                    .flatMap(name -> Arrays.stream(cookies).filter(c -> Objects.equals(c.getName().toLowerCase(), name)))
                    .findFirst()
                    .map(Cookie::getValue);
        }
        return result;
    }

    @Override
    @NotNull
    public RefreshToken generateRefreshToken(JwtUser user) {
        byte[] data = new byte[refreshConfig.getLength()];
        random.nextBytes(data);
        final String token = Base64.getEncoder().encodeToString(data);
        log.debug("Generated refresh token, storing at configured store");
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
