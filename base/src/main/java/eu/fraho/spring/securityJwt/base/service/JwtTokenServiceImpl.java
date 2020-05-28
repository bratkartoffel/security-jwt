/*
 * MIT Licence
 * Copyright (c) 2020 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import eu.fraho.spring.securityJwt.base.config.*;
import eu.fraho.spring.securityJwt.base.dto.AccessToken;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import eu.fraho.spring.securityJwt.base.dto.RefreshToken;
import eu.fraho.spring.securityJwt.base.exceptions.FeatureNotConfiguredException;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
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
@NoArgsConstructor
public class JwtTokenServiceImpl implements JwtTokenService, InitializingBean {
    private final SecureRandom random = new SecureRandom();

    private TokenProperties tokenProperties;

    private RefreshProperties refreshProperties;

    private TokenCookieProperties tokenCookieProperties;

    private TokenHeaderProperties tokenHeaderProperties;

    private RefreshCookieProperties refreshCookieProperties;

    private ObjectFactory<JwtUser> jwtUser;

    private RefreshTokenStore refreshTokenStore;

    @Override
    public void afterPropertiesSet() {
        if (tokenProperties.getSigner() == null) {
            log.warn("No private key specified. This service may neither issue new tokens nor use refresh tokens.");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends JwtUser> Optional<T> parseUser(String token) {
        Optional<T> result = Optional.empty();
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (validateToken(signedJWT)) {
                log.debug("Successfully validated token by client");
                JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
                T user = (T) jwtUser.getObject();
                user.applyClaims(claims);
                log.debug("AbstractToken resulted in user {}", user);
                result = Optional.of(user);
            }
        } catch (ParseException e) {
            log.warn("Could not parse token", e);
        }
        return result;
    }

    @Override

    public <T extends JwtUser> AccessToken generateToken(T user) throws JOSEException {
        if (tokenProperties.getSigner() == null) {
            throw new FeatureNotConfiguredException("Access token signing is not enabled.");
        }

        Date now = new Date();
        JWTClaimsSet claims = user.toClaims()
                .jwtID(UUID.randomUUID().toString())
                .issuer(tokenProperties.getIssuer())
                .issueTime(now)
                .notBeforeTime(now)
                .expirationTime(generateExpirationDate())
                .build();
        SignedJWT token = new SignedJWT(
                new JWSHeader(tokenProperties.getJwsAlgorithm()),
                claims);
        token.sign(tokenProperties.getSigner());

        return AccessToken.builder()
                .token(token.serialize())
                .expiresIn(tokenProperties.getExpiration().toSeconds())
                .build();
    }


    private Date generateExpirationDate() {
        return Date.from(ZonedDateTime.now().plusSeconds(tokenProperties.getExpiration().toSeconds()).toInstant());
    }

    @Override
    public boolean validateToken(AccessToken token) {
        return validateToken(token.getToken());
    }

    @Override
    public boolean validateToken(String token) {
        SignedJWT signedJWT;
        try {
            signedJWT = SignedJWT.parse(token);
        } catch (ParseException e) {
            log.error("Supplied token did not validate", e);
            return false;
        }
        return validateToken(signedJWT);
    }

    @Override
    public boolean validateToken(SignedJWT signedJWT) {
        boolean result;
        try {
            result = signedJWT.verify(tokenProperties.getVerifier());
            log.debug("AbstractToken signature verified, result={}", result);
            result &= areClaimsValid(signedJWT.getJWTClaimsSet());
            log.debug("Claims verified, result={}", result);
        } catch (ParseException | JOSEException e) {
            log.error("Supplied token did not validate", e);
            result = false;
        }
        return result;
    }

    private boolean areClaimsValid(JWTClaimsSet claims) {
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
    public Optional<String> getToken(HttpServletRequest request) {
        return getAccessToken(request);
    }

    @Override
    public Optional<String> getAccessToken(HttpServletRequest request) {
        Optional<String> result = Optional.empty();
        if (tokenHeaderProperties.isEnabled()) {
            log.debug("Extracting token from header");
            result = extractHeaderToken(tokenHeaderProperties.getNames(), request);
        }
        if (!result.isPresent() && tokenCookieProperties.isEnabled()) {
            log.debug("Extracting token from cookies");
            result = extractCookieToken(tokenCookieProperties.getNames(), request.getCookies());
        }
        return result;
    }

    @Override
    public Optional<String> getRefreshToken(HttpServletRequest request) {
        Optional<String> result = Optional.empty();
        if (refreshCookieProperties.isEnabled()) {
            log.debug("Extracting refreshtoken from cookies");
            result = extractCookieToken(refreshCookieProperties.getNames(), request.getCookies());
        }
        return result;
    }

    private Optional<String> extractHeaderToken(String[] names, HttpServletRequest request) {
        return Arrays.stream(names)
                .map(request::getHeader)
                .filter(Objects::nonNull)
                .findFirst()
                .map(e -> e.startsWith("Bearer ") ? e.substring(7) : e);
    }

    private Optional<String> extractCookieToken(String[] names, Cookie[] cookies) {
        Optional<String> result = Optional.empty();
        if (cookies != null) {
            result = Arrays.stream(names)
                    .flatMap(name -> Arrays.stream(cookies).filter(c -> Objects.equals(c.getName(), name)))
                    .findFirst()
                    .map(Cookie::getValue);
        }
        return result;
    }

    @Override

    public RefreshToken generateRefreshToken(JwtUser user) {
        byte[] data = new byte[refreshProperties.getLength()];
        random.nextBytes(data);
        final String token = Base64.getEncoder().encodeToString(data);
        log.debug("Generated refresh token, storing at configured store");
        refreshTokenStore.saveToken(user, token);
        return RefreshToken.builder()
                .token(token)
                .expiresIn(refreshProperties.getExpiration().toSeconds())
                .build();
    }

    @Override
    public boolean isRefreshTokenSupported() {
        return refreshTokenStore.isRefreshTokenSupported();
    }

    @Override
    public <T extends JwtUser> Optional<T> useRefreshToken(RefreshToken token) {
        return useRefreshToken(token.getToken());
    }

    @Override
    public <T extends JwtUser> Optional<T> useRefreshToken(String token) {
        if (tokenProperties.getSigner() == null) {
            throw new FeatureNotConfiguredException("Access token signing is not enabled.");
        }

        return refreshTokenStore.useToken(token);
    }

    @Override

    public Map<Long, List<RefreshToken>> listRefreshTokens() {
        return refreshTokenStore.listTokens();
    }

    @Override

    public List<RefreshToken> listRefreshTokens(JwtUser user) {
        return refreshTokenStore.listTokens(user);
    }

    @Override
    public boolean revokeRefreshToken(RefreshToken token) {
        return refreshTokenStore.revokeToken(token.getToken());
    }

    @Override
    public boolean revokeRefreshToken(String token) {
        return refreshTokenStore.revokeToken(token);
    }

    @Override
    public int revokeRefreshTokens(JwtUser user) {
        return refreshTokenStore.revokeTokens(user);
    }

    @Override
    public int clearTokens() {
        return refreshTokenStore.revokeTokens();
    }

    @Autowired
    public void setTokenProperties(@NonNull TokenProperties tokenProperties) {
        this.tokenProperties = tokenProperties;
    }

    @Autowired
    public void setRefreshProperties(@NonNull RefreshProperties refreshProperties) {
        this.refreshProperties = refreshProperties;
    }

    @Autowired
    public void setTokenCookieProperties(@NonNull TokenCookieProperties tokenCookieProperties) {
        this.tokenCookieProperties = tokenCookieProperties;
    }

    @Autowired
    public void setTokenHeaderProperties(@NonNull TokenHeaderProperties tokenHeaderProperties) {
        this.tokenHeaderProperties = tokenHeaderProperties;
    }

    @Autowired
    public void setRefreshCookieProperties(@NonNull RefreshCookieProperties refreshCookieProperties) {
        this.refreshCookieProperties = refreshCookieProperties;
    }

    @Autowired
    public void setJwtUser(@NonNull ObjectFactory<JwtUser> jwtUser) {
        this.jwtUser = jwtUser;
    }

    @Autowired
    @Lazy
    public void setRefreshTokenStore(@NonNull RefreshTokenStore refreshTokenStore) {
        this.refreshTokenStore = refreshTokenStore;
    }
}
