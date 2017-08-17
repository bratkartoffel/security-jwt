/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.*;
import eu.fraho.spring.securityJwt.dto.TimeWithPeriod;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@ConfigurationProperties(prefix = "fraho.jwt.token")
@Component
@Data
@Slf4j
public class JwtTokenConfiguration implements InitializingBean {
    private TimeWithPeriod expiration = new TimeWithPeriod("1 hour");
    private String algorithm = "ES256";
    private Path pub = null;
    private Path priv = null;
    private Path hmac = null;
    private String issuer = "fraho-security";

    @Setter(AccessLevel.NONE)
    private JWSAlgorithm jwsAlgorithm;

    @Setter(AccessLevel.NONE)
    private transient volatile JWSSigner signer = null;

    @Setter(AccessLevel.NONE)
    private JWSVerifier verifier;

    @Override
    public void afterPropertiesSet() throws Exception {
        JWSAlgorithm jwsAlgorithm = JWSAlgorithm.parse(algorithm);
        if (jwsAlgorithm.getRequirement() == null) {
            throw new IllegalArgumentException("Unknown signature algorithm configured: " + algorithm);
        }
        log.debug("Using signature algorithm: {}", jwsAlgorithm);
        this.jwsAlgorithm = jwsAlgorithm;

        // load the keys
        byte[] publicKeyBytes = new byte[0];
        byte[] privateKeyBytes = new byte[0];
        byte[] hmacSecret = new byte[0];
        if (pub != null) {
            log.debug("Loading public key from: {}", pub);
            publicKeyBytes = Files.readAllBytes(pub);
        }
        if (priv != null) {
            log.debug("Loading private key from: {}", priv);
            privateKeyBytes = Files.readAllBytes(priv);
        }
        if (hmac != null) {
            log.debug("Loading hmac secret from: {}", hmac);
            hmacSecret = Files.readAllBytes(hmac);
        }

        // parse the keys
        signer = null;
        verifier = null;
        if (JWSAlgorithm.Family.EC.contains(jwsAlgorithm)) {
            log.info("Using EC based JWT signature");
            assertKeyPresent(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");

            if (privateKeyBytes.length > 0) {
                PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
                signer = new ECDSASigner((ECPrivateKey) privateKey);
            }

            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
            verifier = new ECDSAVerifier((ECPublicKey) publicKey);
        } else if (JWSAlgorithm.Family.RSA.contains(jwsAlgorithm)) {
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
            if (hmacSecret.length == 0) {
                throw new IllegalArgumentException("No secret key configured.");
            }
            verifier = new MACVerifier(hmacSecret);
            signer = new MACSigner(hmacSecret);
        }
    }

    private void assertKeyPresent(byte[] key) {
        if (key.length == 0) {
            throw new IllegalArgumentException("No public key configured.");
        }
    }
}
