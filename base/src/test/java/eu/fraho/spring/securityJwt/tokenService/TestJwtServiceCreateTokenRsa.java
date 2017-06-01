/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.tokenService;

import eu.fraho.spring.securityJwt.spring.TestApiApplication;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Getter
@Setter(AccessLevel.NONE)
@Slf4j
@SpringBootTest(properties = "spring.config.location=classpath:test-rsa.yaml",
        classes = TestApiApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TestJwtServiceCreateTokenRsa extends AbstractCreateTokenTest {
    public static final String OUT_PUB_KEY = "build/rsa.pub";
    public static final String OUT_PRIV_KEY = "build/rsa.priv";

    @BeforeClass
    public static void beforeClass() throws Exception {
        checkAndCreateOutDirs(OUT_PUB_KEY);
        checkAndCreateOutDirs(OUT_PRIV_KEY);

        log.info("Initializing KeyPairGenerator");
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGenerator.initialize(512);

        // generate the key pair
        log.info("Generating keypair");
        KeyPair keyPair = keyPairGenerator.genKeyPair();

        // create KeyFactory and RSA Keys Specs
        log.info("Extracting private / public keys");
        KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(keyPair.getPublic().getEncoded()));
        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded()));

        log.info("Writing public key to {}", OUT_PUB_KEY);
        Files.write(Paths.get(OUT_PUB_KEY), publicKey.getEncoded());
        log.info("Writing private key to {}", OUT_PRIV_KEY);
        Files.write(Paths.get(OUT_PRIV_KEY), privateKey.getEncoded());
    }
}
