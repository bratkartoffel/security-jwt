/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.util;

import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
public class CreateEcdsaJwtKeys {
    private static final String OUT_PUB_KEY = "build/pub.key";
    private static final String OUT_PRIV_KEY = "build/priv.key";
    private static final String OUT_PUB_JWK = "build/jwk_pub.json";
    private static final String OUT_PRIV_JWK = "build/jwk_priv.json";
    private static final ECKey.Curve CURVE = ECKey.Curve.P_256;

    public static void main(String[] args) throws Throwable {
        Security.addProvider(new BouncyCastleProvider());

        checkAndCreateOutDirs(OUT_PRIV_KEY);
        checkAndCreateOutDirs(OUT_PUB_KEY);
        checkAndCreateOutDirs(OUT_PUB_JWK);

        write();
        read();
    }

    public static void checkAndCreateOutDirs(String path) throws NoSuchFileException {
        final File parent = Paths.get(path).getParent().toFile();
        if (!parent.exists()) {
            log.info("Creating output directory: {}", parent.getAbsolutePath());
            if (!parent.mkdirs()) {
                log.error("Could not create directory");
                throw new NoSuchFileException("Could not create directory: " + parent.getAbsolutePath());
            }
        }
    }

    private static void write() throws Throwable {
        log.info("***** START write() *****");
        log.info("Initializing KeyPairGenerator");
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
        keyPairGenerator.initialize(CURVE.toECParameterSpec());

        // generate the key pair
        log.info("Generating keypair");
        KeyPair keyPair = keyPairGenerator.genKeyPair();

        // create KeyFactory and RSA Keys Specs
        log.info("Extracting private / public keys");
        KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(keyPair.getPublic().getEncoded()));
        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded()));

        log.info("Writing public key to {}", OUT_PUB_KEY);
        Files.write(Paths.get(OUT_PUB_KEY), publicKey.getEncoded());
        log.info("Writing private key to {}", OUT_PRIV_KEY);
        Files.write(Paths.get(OUT_PRIV_KEY), privateKey.getEncoded());

        log.info("Creating JWK");
        JWK jwk = new ECKey.Builder(CURVE, (ECPublicKey) publicKey).privateKey((ECPrivateKey) privateKey).build();

        log.info("Writing public jwk to {}", OUT_PUB_JWK);
        Files.write(Paths.get(OUT_PUB_JWK), jwk.toPublicJWK().toJSONString().getBytes(StandardCharsets.UTF_8));

        log.info("Writing private jwk to {}", OUT_PRIV_JWK);
        Files.write(Paths.get(OUT_PRIV_JWK), jwk.toJSONString().getBytes(StandardCharsets.UTF_8));
        log.info("***** END write() *****");
    }

    private static void read() throws Throwable {
        log.info("***** START read() *****");
        log.info("Initializing KeyFactory");
        KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");

        log.info("Loading public key from {}", OUT_PUB_KEY);
        byte[] publicKeySpec = Files.readAllBytes(Paths.get(OUT_PUB_KEY));

        log.info("Loading private key from {}", OUT_PRIV_KEY);
        byte[] privateKeySpec = Files.readAllBytes(Paths.get(OUT_PRIV_KEY));

        log.info("Loading public jwk from {}", OUT_PUB_JWK);
        byte[] publicJwk = Files.readAllBytes(Paths.get(OUT_PUB_JWK));

        log.info("Loading private jwk from {}", OUT_PRIV_JWK);
        byte[] privateJwk = Files.readAllBytes(Paths.get(OUT_PRIV_JWK));

        log.info("Parsing private / public keys");
        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeySpec));
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeySpec));

        Base64.Encoder encoder = Base64.getEncoder();
        log.info("Encoded public key:  {}", encoder.encodeToString(publicKey.getEncoded()));
        log.info("Encoded private key: {}", encoder.encodeToString(privateKey.getEncoded()));
        log.info("Public JWK:  {}", new String(publicJwk, StandardCharsets.UTF_8));
        log.info("Private JWK: {}", new String(privateJwk, StandardCharsets.UTF_8));

        log.info("***** END read() *****");
    }
}
