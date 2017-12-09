/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.ut.config;

import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.ECKey;
import eu.fraho.spring.securityJwt.config.TokenProperties;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
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

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownAlgorithm() {
        TokenProperties conf = getNewInstance();
        conf.setAlgorithm("foobar");
        try {
            conf.afterPropertiesSet();
        } catch (IllegalArgumentException iae) {
            Assert.assertEquals("Wrong exception text", "Unknown signature algorithm configured: foobar", iae.getMessage());
            throw iae;
        }
    }

    @Test
    public void testHmacNoKey() {
        TokenProperties conf = getNewInstance();
        conf.afterPropertiesSet();

        Assert.assertEquals("Verifier should be for HMAC", MACVerifier.class, conf.getVerifier().getClass());
        Assert.assertEquals("Signer should be for HMAC", MACSigner.class, conf.getSigner().getClass());
    }

    @Test
    public void testHmacEmptyKeyfile() throws Exception {
        try (OutputStream os = new FileOutputStream(tempPrivate)) {
            os.flush();
        }
        TokenProperties conf = getNewInstance();
        conf.setHmac(tempPrivate.toPath());
        conf.afterPropertiesSet();

        Assert.assertEquals("Verifier should be for HMAC", MACVerifier.class, conf.getVerifier().getClass());
        Assert.assertEquals("Signer should be for HMAC", MACSigner.class, conf.getSigner().getClass());
    }

    @Test(expected = KeyLengthException.class)
    public void testHmacSmallKeyfile() throws Throwable {
        try (OutputStream os = new FileOutputStream(tempPrivate)) {
            os.write(42);
        }
        TokenProperties conf = getNewInstance();
        conf.setHmac(tempPrivate.toPath());
        try {
            conf.afterPropertiesSet();
        } catch (IllegalArgumentException iae) {
            throw iae.getCause();
        }
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

        Assert.assertEquals("Verifier should be for HMAC", MACVerifier.class, conf.getVerifier().getClass());
        Assert.assertArrayEquals("Secret not loaded", secret, ((MACVerifier) conf.getVerifier()).getSecret());
    }

    @Test
    public void testRsa() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        try {
            TokenProperties conf = withRsa(getNewInstance());
            conf.afterPropertiesSet();

            Assert.assertEquals("Verifier should be for RSA", RSASSAVerifier.class, conf.getVerifier().getClass());
            Assert.assertEquals("Signer should be for RSA", RSASSASigner.class, conf.getSigner().getClass());
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

        Assert.assertEquals("Verifier should be for RSA", RSASSAVerifier.class, conf.getVerifier().getClass());
        Assert.assertNull("Signer should be null", conf.getSigner());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRsaNoPubkey() throws Exception {
        TokenProperties conf = withRsa(getNewInstance());
        try (OutputStream os = new FileOutputStream(tempPublic)) {
            os.flush();
        }
        try {
            conf.afterPropertiesSet();
        } catch (IllegalArgumentException iae) {
            Assert.assertEquals("Wrong exception text", "No public key configured", iae.getMessage());
            throw iae;
        }
    }

    @Test
    public void testEcdsa() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        try {
            TokenProperties conf = withEcdsa(getNewInstance());
            conf.afterPropertiesSet();

            Assert.assertEquals("Verifier should be for ECDSA", ECDSAVerifier.class, conf.getVerifier().getClass());
            Assert.assertEquals("Signer should be for ECDSA", ECDSASigner.class, conf.getSigner().getClass());
        } finally {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEcdsaNotSupported() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        TokenProperties conf;
        try {
            conf = withEcdsa(getNewInstance());
        } finally {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
        try {
            conf.afterPropertiesSet();
        } catch (IllegalArgumentException iae) {
            Assert.assertEquals("Wrong exception text",
                    "Bouncycastle is not installed properly, ECDSA is not available!",
                    iae.getMessage());
            throw iae;
        }
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

            Assert.assertEquals("Verifier should be for ECDSA", ECDSAVerifier.class, conf.getVerifier().getClass());
            Assert.assertNull("Signer should be null", conf.getSigner());
        } finally {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEcsaNoPubkey() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        try {
            TokenProperties conf = withEcdsa(getNewInstance());
            try (OutputStream os = new FileOutputStream(tempPublic)) {
                os.flush();
            }
            try {
                conf.afterPropertiesSet();
            } catch (IllegalArgumentException iae) {
                Assert.assertEquals("Wrong exception text", "No public key configured", iae.getMessage());
                throw iae;
            }
        } finally {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNeitherHeaderNorCookieEnabled() {
        TokenProperties conf = getNewInstance();
        conf.getCookie().setEnabled(false);
        conf.getHeader().setEnabled(false);
        try {
            conf.afterPropertiesSet();
        } catch (IllegalArgumentException iae) {
            Assert.assertEquals("Wrong exception text",
                    "Please enable at least one of header or cookie authentication.", iae.getMessage());
            throw iae;
        }
    }

    private TokenProperties withRsa(final TokenProperties conf) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        // initialize generator
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(512);

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
        keyPairGenerator.initialize(ECKey.Curve.P_256.toECParameterSpec());

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
