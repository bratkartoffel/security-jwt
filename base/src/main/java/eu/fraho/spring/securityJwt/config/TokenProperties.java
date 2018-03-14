/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.*;
import eu.fraho.spring.securityJwt.dto.TimeWithPeriod;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@ConfigurationProperties(prefix = "fraho.jwt.token")
@Component
@Getter
@Setter
@Slf4j
public class TokenProperties implements InitializingBean {
    /**
     * How long are access tokens valid? For details please on how to specifiy this value please see the
     * documentation of the value class behind this field.
     */
    @NotNull
    @NonNull
    private TimeWithPeriod expiration = new TimeWithPeriod("1 hour");

    /**
     * The signature algorithm used for the tokens. For a list of valid algorithms please see either the
     * <a href="https://tools.ietf.org/html/rfc7518#section-3">JWT spec</a> or
     * <a href="https://bitbucket.org/connect2id/nimbus-jose-jwt/src/master/src/main/java/com/nimbusds/jose/JWSAlgorithm.java">JWSAlgorithm</a>
     */
    @NotNull
    @NonNull
    private String algorithm = "HS256";

    /**
     * Defines the public key file when using a public / private key signature method
     */
    @Nullable
    private Path pub;

    /**
     * Defines the private key file when using a public / private key signature method.
     * May be null if this service should only verify, but not issue tokens.<br>
     * In this case, any calls to generateToken or generateRefreshToken will throw an FeatureNotConfiguredException.
     * To the caller, it will be shown as a UNAUTHORIZED Http StatusCode.
     */
    @Nullable
    private Path priv;

    /**
     * Defines the key file when using a hmac signature method
     */
    @Nullable
    private Path hmac;

    /**
     * Sets the issuer of the token. The issuer is used in the tokens iss field
     */
    @NotNull
    @NonNull
    private String issuer = "fraho-security";

    /**
     * Sets the path for the RestController, defining the endpoint for login requests.
     */
    @NotNull
    @NonNull
    private String path = "/auth/login";

    @Setter(AccessLevel.NONE)
    private JWSAlgorithm jwsAlgorithm;

    @Setter(AccessLevel.NONE)
    private transient volatile JWSSigner signer = null;

    @Setter(AccessLevel.NONE)
    private JWSVerifier verifier;

    @NestedConfigurationProperty
    private TokenHeaderProperties header = new TokenHeaderProperties();

    @NestedConfigurationProperty
    private TokenCookieProperties cookie = new TokenCookieProperties();

    private void tryLoadHmac() throws IOException, JOSEException {
        byte[] hmacSecret = new byte[0];
        if (hmac != null) {
            log.debug("Loading hmac secret from: {}", hmac);
            hmacSecret = Files.readAllBytes(hmac);
        }
        log.info("Using HMAC based JWT signature");
        if (hmacSecret.length == 0) {
            log.warn("No secret keyfile has been specified, creating a new random one. Tokens will not be valid accross server restarts!");
            SecureRandom random = new SecureRandom();
            hmacSecret = new byte[48];
            random.nextBytes(hmacSecret);
        }
        verifier = new MACVerifier(hmacSecret);
        signer = new MACSigner(hmacSecret);
    }

    private void tryLoadRsa() throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicKeyBytes = new byte[0];
        byte[] privateKeyBytes = new byte[0];
        if (pub != null) {
            log.debug("Loading public key from: {}", pub);
            publicKeyBytes = Files.readAllBytes(pub);
        }
        if (priv != null) {
            log.debug("Loading private key from: {}", priv);
            privateKeyBytes = Files.readAllBytes(priv);
        }

        log.info("Using RSA based JWT signature");
        assertKeyPresent(publicKeyBytes);
        KeyFactory keyFactory = getKeyFactory("RSA");

        if (privateKeyBytes.length > 0) {
            PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
            signer = new RSASSASigner(privateKey);
        }

        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        verifier = new RSASSAVerifier((RSAPublicKey) publicKey);
    }

    private void tryLoadEcdsa() throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, JOSEException {
        byte[] publicKeyBytes = new byte[0];
        byte[] privateKeyBytes = new byte[0];
        if (pub != null) {
            log.debug("Loading public key from: {}", pub);
            publicKeyBytes = Files.readAllBytes(pub);
        }
        if (priv != null) {
            log.debug("Loading private key from: {}", priv);
            privateKeyBytes = Files.readAllBytes(priv);
        }
        log.info("Using EC based JWT signature");
        assertKeyPresent(publicKeyBytes);
        KeyFactory keyFactory = getKeyFactory("ECDSA");

        if (privateKeyBytes.length > 0) {
            PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
            signer = new ECDSASigner((ECPrivateKey) privateKey);
        }

        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        verifier = new ECDSAVerifier((ECPublicKey) publicKey);
    }

    @NotNull
    private KeyFactory getKeyFactory(@NotNull String algorithm) throws NoSuchAlgorithmException, NoSuchProviderException {
        final KeyFactory keyFactory;
        if (Security.getProvider("BC") == null) {
            log.warn("BouncyCastle provider is not available, trying to use java builtin provider. ECDSA will not be available");
            keyFactory = KeyFactory.getInstance(algorithm);
        } else {
            keyFactory = KeyFactory.getInstance(algorithm, "BC");
        }
        return keyFactory;
    }

    @Override
    public void afterPropertiesSet() {
        JWSAlgorithm jwsAlgorithm = JWSAlgorithm.parse(algorithm);
        if (jwsAlgorithm.getRequirement() == null) {
            throw new IllegalArgumentException("Unknown signature algorithm configured: " + algorithm);
        }
        log.debug("Using signature algorithm: {}", jwsAlgorithm);
        this.jwsAlgorithm = jwsAlgorithm;

        // check if at least one of cookie or header authentication is enabled
        if (!header.isEnabled() && !cookie.isEnabled()) {
            throw new IllegalArgumentException("Please enable at least one of header or cookie authentication.");
        }

        // load the keys
        signer = null;
        verifier = null;
        try {
            if (JWSAlgorithm.Family.EC.contains(jwsAlgorithm)) {
                if (Security.getProvider("BC") == null) {
                    throw new IllegalArgumentException("Bouncycastle is not installed properly, ECDSA is not available!");
                }

                tryLoadEcdsa();
            } else if (JWSAlgorithm.Family.RSA.contains(jwsAlgorithm)) {
                tryLoadRsa();
            } else {
                tryLoadHmac();
            }
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to load keys", ex);
        }
    }

    private void assertKeyPresent(@NotNull byte[] key) {
        if (key.length == 0) {
            throw new IllegalArgumentException("No public key configured");
        }
    }
}
