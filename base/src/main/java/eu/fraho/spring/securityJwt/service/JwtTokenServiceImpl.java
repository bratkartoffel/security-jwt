/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import eu.fraho.spring.securityJwt.dto.AccessToken;
import eu.fraho.spring.securityJwt.dto.JwtUser;
import eu.fraho.spring.securityJwt.dto.RefreshToken;
import eu.fraho.spring.securityJwt.dto.TimeWithPeriod;
import eu.fraho.spring.securityJwt.exceptions.FeatureNotConfiguredException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.*;

@Component
@Slf4j
public class JwtTokenServiceImpl implements JwtTokenService, InitializingBean {
    private final SecureRandom random = new SecureRandom();
    @Autowired
    private AutowireCapableBeanFactory autowireCapableBeanFactory = null;
    @Value("${fraho.jwt.token.algorithm:" + DEFAULT_ALGORITHM + "}")
    private String algorithm = DEFAULT_ALGORITHM;
    @Value("${fraho.jwt.token.issuer:" + DEFAULT_ISSUER + "}")
    private String issuer = null;
    @Value("${fraho.jwt.token.pub:#{null}}")
    private Path pubKey = null;
    @Value("${fraho.jwt.token.priv:#{null}}")
    private Path privKey = null;
    @Value("${fraho.jwt.token.hmac:#{null}}")
    private Path hmacKey = null;
    @Value("${fraho.jwt.token.expiration:" + DEFAULT_EXPIRATION + "}")
    private TimeWithPeriod tokenExpiration = new TimeWithPeriod(DEFAULT_EXPIRATION);
    @Value("${fraho.jwt.refresh.length:" + REFRESH_TOKEN_LEN_DEFAULT + "}")
    private Integer refreshLength = REFRESH_TOKEN_LEN_DEFAULT;
    @Value("${fraho.jwt.refresh.deviceIdLength:" + DEFAULT_MAX_DEVICE_ID_LENGTH + "}")
    private Integer maxDeviceIdLength = DEFAULT_MAX_DEVICE_ID_LENGTH;
    @Value("${fraho.jwt.refresh.cache.impl:" + DEFAULT_CACHE_IMPL + "}")
    private Class<? extends RefreshTokenStore> refreshTokenStoreImpl = null;
    private RefreshTokenStore refreshTokenStore = null;
    private transient volatile JWSSigner signer = null;
    private JWSVerifier verifier;
    private JWSAlgorithm signatureAlgorithm;

    private String truncateDeviceId(String str) {
        final String trimmedId = str == null ? DEFAULT_DEVICE_ID : str.trim();
        final String deviceId = trimmedId.isEmpty() ? DEFAULT_DEVICE_ID : trimmedId;
        return deviceId.substring(0, Math.min(deviceId.length(), maxDeviceIdLength));
    }

    @Override
    public Integer getExpiration() {
        return tokenExpiration.toSeconds();
    }

    @Override
    public Integer getRefreshExpiration() {
        return refreshTokenStore.getRefreshExpiration().toSeconds();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.debug("Initializing");
        signer = null;
        verifier = null;

        // parse the signature algorithm
        signatureAlgorithm = JWSAlgorithm.parse(algorithm);
        if (signatureAlgorithm.getRequirement() == null) {
            throw new IllegalArgumentException("Unknown signature algorithm configured: " + algorithm);
        }
        log.debug("Using signature algorithm: {}", algorithm);

        // check refresh token length
        if (refreshLength < REFRESH_TOKEN_LEN_MIN || refreshLength > REFRESH_TOKEN_LEN_MAX) {
            log.warn("Refresh token length ({} <= {} <= {}), forcing to default ({})",
                    REFRESH_TOKEN_LEN_MIN, refreshLength, REFRESH_TOKEN_LEN_MAX, REFRESH_TOKEN_LEN_DEFAULT);
            refreshLength = REFRESH_TOKEN_LEN_DEFAULT;
        }

        // load the keys
        byte[] publicKeyBytes = new byte[0];
        byte[] privateKeyBytes = new byte[0];
        byte[] hmacSecret = new byte[0];
        if (pubKey != null) {
            log.debug("Loading public key from: {}", pubKey);
            publicKeyBytes = Files.readAllBytes(pubKey);
        }
        if (privKey != null) {
            log.debug("Loading private key from: {}", privKey);
            privateKeyBytes = Files.readAllBytes(privKey);
        }
        if (hmacKey != null) {
            log.debug("Loading hmac secret from: {}", hmacKey);
            hmacSecret = Files.readAllBytes(hmacKey);
        }

        // parse the keys
        if (JWSAlgorithm.Family.EC.contains(signatureAlgorithm)) {
            log.info("Using EC based JWT signature");
            assertKeyPresent(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");

            if (privateKeyBytes.length > 0) {
                PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
                signer = new ECDSASigner((ECPrivateKey) privateKey);
            }

            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
            verifier = new ECDSAVerifier((ECPublicKey) publicKey);
        } else if (JWSAlgorithm.Family.RSA.contains(signatureAlgorithm)) {
            log.info("Using RSA based JWT signature");
            assertKeyPresent(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");

            if (privateKeyBytes.length > 0) {
                PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
                signer = new RSASSASigner(privateKey);
            }

            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
            verifier = new RSASSAVerifier((RSAPublicKey) publicKey);
        } else {
            log.info("Using HMAC based JWT signature");
            if (hmacSecret == null || hmacSecret.length == 0) {
                throw new IllegalArgumentException("No secret key configured.");
            }
            verifier = new MACVerifier(hmacSecret);
            signer = new MACSigner(hmacSecret);
        }

        if (signer == null) {
            log.warn("No private key specified. This service may neither issue new tokens nor use refresh tokens.");
        }

        if (refreshTokenStoreImpl != null) {
            log.debug("Using refresh token store implementation: {}", refreshTokenStoreImpl);
            refreshTokenStore = refreshTokenStoreImpl.newInstance();
            autowireCapableBeanFactory.autowireBean(refreshTokenStore);
            refreshTokenStore.afterPropertiesSet();
        } else {
            log.debug("Disabling refresh token store, no implementation specified");
            refreshTokenStore = new NullTokenStore();
        }
    }

    private void assertKeyPresent(byte[] publicKeyBytes) {
        if (publicKeyBytes.length == 0) {
            throw new IllegalArgumentException("No public key configured.");
        }
    }

    @Override
    public Optional<JwtUser> parseUser(String token) {
        try {
            return Optional.of(JwtUser.fromClaims(SignedJWT.parse(token).getJWTClaimsSet()));
        } catch (ParseException e) {
            return Optional.empty();
        }
    }

    @Override
    public AccessToken generateToken(JwtUser user) throws JOSEException {
        if (signer == null) {
            throw new FeatureNotConfiguredException("Access token signing is not enabled.");
        }

        Date now = new Date();
        JWTClaimsSet claims = user.toClaims()
                .jwtID(UUID.randomUUID().toString())
                .issuer(issuer)
                .issueTime(now)
                .notBeforeTime(now)
                .expirationTime(generateExpirationDate())
                .build();

        SignedJWT token = new SignedJWT(
                new JWSHeader(signatureAlgorithm),
                claims);

        token.sign(signer);
        return new AccessToken(token.serialize(), getExpiration());
    }

    private Date generateExpirationDate() {
        return Date.from(ZonedDateTime.now().plusSeconds(tokenExpiration.toSeconds()).toInstant());
    }

    @Override
    public boolean validateToken(String token) {
        boolean result;
        try {
            SignedJWT parsedToken = SignedJWT.parse(token);
            JWTClaimsSet claims = parsedToken.getJWTClaimsSet();
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

            result = parsedToken.verify(verifier);
            result &= iat.before(now);
            result &= nbf.before(now);
            result &= exp.after(now);
        } catch (ParseException | JOSEException e) {
            result = false;
        }

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
    public RefreshToken generateRefreshToken(String user) {
        return generateRefreshToken(user, DEFAULT_DEVICE_ID);
    }

    @Override
    public RefreshToken generateRefreshToken(String user, String deviceId) {
        final String devId = truncateDeviceId(deviceId);
        byte[] data = new byte[refreshLength];
        random.nextBytes(data);
        final String token = Base64.getEncoder().encodeToString(data);

        refreshTokenStore.saveToken(user, devId, token);
        return new RefreshToken(token, getRefreshExpiration(), devId);
    }

    @Override
    public boolean useRefreshToken(String username, String refreshToken) {
        return useRefreshToken(username, DEFAULT_DEVICE_ID, refreshToken);
    }

    @Override
    public boolean useRefreshToken(String username, RefreshToken token) {
        return useRefreshToken(username, token.getDeviceId(), token.getToken());
    }

    @Override
    public boolean useRefreshToken(String username, String deviceId, String refreshToken) {
        if (signer == null) {
            throw new FeatureNotConfiguredException("Access token signing is not enabled.");
        }

        final String devId = truncateDeviceId(deviceId);
        return refreshTokenStore.useToken(username, devId, refreshToken);
    }

    @Override
    public Map<String, List<RefreshToken>> listRefreshTokens() {
        return refreshTokenStore.listTokens();
    }

    @Override
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

    Class<? extends RefreshTokenStore> getInternalRefreshTokenStoreType() {
        return refreshTokenStoreImpl;
    }

    RefreshTokenStore getInternalRefreshTokenStore() {
        return refreshTokenStore;
    }
}
