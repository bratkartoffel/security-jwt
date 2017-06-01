package eu.fraho.spring.securityJwt.tokenService;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.ECKey;
import eu.fraho.spring.securityJwt.dto.RefreshToken;
import eu.fraho.spring.securityJwt.exceptions.FeatureNotConfiguredException;
import eu.fraho.spring.securityJwt.spring.TestApiApplication;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

@Getter
@Setter(AccessLevel.NONE)
@Slf4j
@SpringBootTest(properties = "spring.config.location=classpath:test-only-verify.yaml",
        classes = TestApiApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TestJwtServiceOnlyVerify extends AbstractCreateTokenTest {
    public static final String OUT_PUB_KEY = "build/ecdsa.pub";

    @BeforeClass
    public static void beforeClass() throws Exception {
        checkAndCreateOutDirs(OUT_PUB_KEY);

        log.info("Initializing KeyPairGenerator");
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
        keyPairGenerator.initialize(ECKey.Curve.P_256.toECParameterSpec());

        // generate the key pair
        log.info("Generating keypair");
        KeyPair keyPair = keyPairGenerator.genKeyPair();

        // create KeyFactory and RSA Keys Specs
        log.info("Extracting private / public keys");
        KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(keyPair.getPublic().getEncoded()));

        log.info("Writing public key to {}", OUT_PUB_KEY);
        Files.write(Paths.get(OUT_PUB_KEY), publicKey.getEncoded());
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testCreateToken() throws JOSEException {
        super.testCreateToken();
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testRefreshToken() throws JOSEException {
        jwtTokenService.useRefreshToken("john", new RefreshToken("foobar", 1, "none"));
    }
}
