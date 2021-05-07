/*
 * MIT Licence
 * Copyright (c) 2021 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.tests.ut.config;

import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.Curve;
import eu.fraho.spring.securityJwt.base.config.TokenProperties;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class TokenPropertiesTest {
    /**
     * private key or hmac secret
     */
    private final File tempPrivate;

    /**
     * public key
     */
    private final File tempPublic;

    public TokenPropertiesTest() throws IOException {
        super();
        tempPrivate = File.createTempFile("security-jwt-", ".tmp");
        tempPublic = File.createTempFile("security-jwt-", ".tmp");
    }

    private TokenProperties getNewInstance() {
        return new TokenProperties();
    }

    @Test
    public void testDefaultConfig() {
        getNewInstance().afterPropertiesSet();
    }

    @Test
    public void testUnknownAlgorithm() {
        TokenProperties conf = getNewInstance();
        conf.setAlgorithm("foobar");
        Assertions.assertThrows(IllegalArgumentException.class, conf::afterPropertiesSet);
    }

    @Test
    public void testHmacNoKey() {
        TokenProperties conf = getNewInstance();
        conf.afterPropertiesSet();

        Assertions.assertEquals(MACVerifier.class, conf.getVerifier().getClass(), "Verifier should be for HMAC");
        Assertions.assertEquals(MACSigner.class, conf.getSigner().getClass(), "Signer should be for HMAC");
    }

    @Test
    public void testHmacEmptyKeyfile() throws Exception {
        try (OutputStream os = new FileOutputStream(tempPrivate)) {
            os.flush();
        }
        TokenProperties conf = getNewInstance();
        conf.setHmac(tempPrivate.toPath());
        conf.afterPropertiesSet();

        Assertions.assertEquals(MACVerifier.class, conf.getVerifier().getClass(), "Verifier should be for HMAC");
        Assertions.assertEquals(MACSigner.class, conf.getSigner().getClass(), "Signer should be for HMAC");
    }

    @Test
    public void testHmacSmallKeyfile() throws Throwable {
        try (OutputStream os = new FileOutputStream(tempPrivate)) {
            os.write(42);
        }
        TokenProperties conf = getNewInstance();
        conf.setHmac(tempPrivate.toPath());
        Assertions.assertThrows(IllegalArgumentException.class, conf::afterPropertiesSet);
    }

    @Test
    public void testHmacLoadKeyfile() throws Exception {
        byte[] secret = "This is just a simple test to ensure that keyfiles are actually loaded".getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = new FileOutputStream(tempPrivate)) {
            os.write(secret);
        }
        TokenProperties conf = getNewInstance();
        conf.setHmac(tempPrivate.toPath());
        conf.afterPropertiesSet();

        Assertions.assertEquals(MACVerifier.class, conf.getVerifier().getClass(), "Verifier should be for HMAC");
        Assertions.assertArrayEquals(secret, ((MACVerifier) conf.getVerifier()).getSecret(), "Secret not loaded");
    }

    @Test
    public void testRsa() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        try {
            TokenProperties conf = withRsa(getNewInstance());
            conf.afterPropertiesSet();

            Assertions.assertEquals(RSASSAVerifier.class, conf.getVerifier().getClass(), "Verifier should be for RSA");
            Assertions.assertEquals(RSASSASigner.class, conf.getSigner().getClass(), "Signer should be for RSA");
        } finally {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    @Test
    public void testRsaNoPrivkey() throws Exception {
        TokenProperties conf = withRsa(getNewInstance());
        try (OutputStream os = new FileOutputStream(tempPrivate)) {
            os.flush();
        }
        conf.afterPropertiesSet();

        Assertions.assertEquals(RSASSAVerifier.class, conf.getVerifier().getClass(), "Verifier should be for RSA");
        Assertions.assertNull(conf.getSigner(), "Signer should be null");
    }

    @Test
    public void testRsaNoPubkey() throws Exception {
        TokenProperties conf = withRsa(getNewInstance());
        try (OutputStream os = new FileOutputStream(tempPublic)) {
            os.flush();
        }
        Assertions.assertThrows(IllegalArgumentException.class, conf::afterPropertiesSet);
    }

    @Test
    public void testEcdsa() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        try {
            TokenProperties conf = withEcdsa(getNewInstance());
            conf.afterPropertiesSet();

            Assertions.assertEquals(ECDSAVerifier.class, conf.getVerifier().getClass(), "Verifier should be for ECDSA");
            Assertions.assertEquals(ECDSASigner.class, conf.getSigner().getClass(), "Signer should be for ECDSA");
        } finally {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    @Test
    public void testEcdsaNotSupported() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        TokenProperties conf;
        try {
            conf = withEcdsa(getNewInstance());
        } finally {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
        Assertions.assertThrows(IllegalArgumentException.class, conf::afterPropertiesSet);
    }

    @Test
    public void testEcsaNoPrivkey() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        try {
            TokenProperties conf = withEcdsa(getNewInstance());
            try (OutputStream os = new FileOutputStream(tempPrivate)) {
                os.flush();
            }
            conf.afterPropertiesSet();

            Assertions.assertEquals(ECDSAVerifier.class, conf.getVerifier().getClass(), "Verifier should be for ECDSA");
            Assertions.assertNull(conf.getSigner(), "Signer should be null");
        } finally {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    @Test
    public void testEcsaNoPubkey() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        try {
            TokenProperties conf = withEcdsa(getNewInstance());
            try (OutputStream os = new FileOutputStream(tempPublic)) {
                os.flush();
            }
            Assertions.assertThrows(IllegalArgumentException.class, conf::afterPropertiesSet);
        } finally {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    @Test
    public void testNeitherHeaderNorCookieEnabled() {
        TokenProperties conf = getNewInstance();
        conf.getCookie().setEnabled(false);
        conf.getHeader().setEnabled(false);
        Assertions.assertThrows(IllegalArgumentException.class, conf::afterPropertiesSet);
    }

    private TokenProperties withRsa(final TokenProperties conf) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        // initialize generator
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);

        // generate the key pair
        KeyPair keyPair = keyPairGenerator.genKeyPair();

        // create KeyFactory and RSA Keys Specs
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(keyPair.getPublic().getEncoded()));
        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded()));

        // write the keys
        Files.write(tempPublic.toPath(), publicKey.getEncoded());
        Files.write(tempPrivate.toPath(), privateKey.getEncoded());

        // adjust configuration
        conf.setAlgorithm("RS256");
        conf.setPub(tempPublic.toPath());
        conf.setPriv(tempPrivate.toPath());
        return conf;
    }

    private TokenProperties withEcdsa(final TokenProperties conf) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidAlgorithmParameterException {
        // initialize generator
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA");
        keyPairGenerator.initialize(Curve.P_256.toECParameterSpec());

        // generate the key pair
        KeyPair keyPair = keyPairGenerator.genKeyPair();

        // create KeyFactory and RSA Keys Specs
        KeyFactory keyFactory = KeyFactory.getInstance("ECDSA");
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(keyPair.getPublic().getEncoded()));
        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded()));

        // write the keys
        Files.write(tempPublic.toPath(), publicKey.getEncoded());
        Files.write(tempPrivate.toPath(), privateKey.getEncoded());

        // adjust configuration
        conf.setAlgorithm("ES256");
        conf.setPub(tempPublic.toPath());
        conf.setPriv(tempPrivate.toPath());
        return conf;
    }
}
