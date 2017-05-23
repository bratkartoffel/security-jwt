package eu.fraho.spring.securityJwt.tokenService;

import com.nimbusds.jose.JOSEException;
import eu.fraho.spring.securityJwt.AbstractTest;
import eu.fraho.spring.securityJwt.dto.AccessToken;
import eu.fraho.spring.securityJwt.dto.JwtUser;
import eu.fraho.spring.securityJwt.service.JwtTokenService;
import eu.fraho.spring.securityJwt.service.JwtTokenServiceImpl;
import eu.fraho.spring.securityJwt.spring.TestApiApplication;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.Optional;

@Getter
@Setter(AccessLevel.NONE)
@Slf4j
@SpringBootTest(properties = "spring.config.location=classpath:test-other.yaml",
        classes = TestApiApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TestJwtServiceOther extends AbstractTest {
    @Autowired
    protected JwtTokenService jwtTokenService = null;

    @BeforeClass
    public static void beforeClass() throws Exception {
        AbstractTest.beforeHmacClass();
    }

    @Test
    public void testParseUser() throws JOSEException {
        JwtUser userIn = getJwtUser();
        AccessToken token = jwtTokenService.generateToken(userIn);
        Assert.assertNotNull("No token generated", token.getToken());

        Optional<JwtUser> tmpUserOut = jwtTokenService.parseUser(token.getToken());
        Assert.assertTrue("User could not be parsed from token", tmpUserOut.isPresent());

        JwtUser userOut = tmpUserOut.get();
        Assert.assertNotNull("Parsed user without id", userOut.getId());
        Assert.assertEquals("Parsed user with wrong id", userIn.getId(), userOut.getId());

        Assert.assertNotNull("Parsed user without username", userOut.getUsername());
        Assert.assertEquals("Parsed user with wrong username", userIn.getUsername(), userOut.getUsername());

        Assert.assertNotNull("Parsed user without role", userOut.getAuthority());
        Assert.assertEquals("Parsed user with wrong role", userIn.getAuthority(), userOut.getAuthority());
    }

    @Test
    public void testGetToken() throws JOSEException {
        HttpServletRequest req;

        req = MockMvcRequestBuilders.get("/").header("Authorization", "Bearer foobar").buildRequest(null);
        Optional<String> token = jwtTokenService.getToken(req);
        Assert.assertTrue("Could not extract token from header", token.isPresent());
        Assert.assertEquals("Could not extract token from header", "foobar", token.get());

        req = MockMvcRequestBuilders.get("/").header("Authorization", "foobar").buildRequest(null);
        token = jwtTokenService.getToken(req);
        Assert.assertTrue("Could not extract token from header", token.isPresent());
        Assert.assertEquals("Could not extract token from header", "foobar", token.get());

        req = MockMvcRequestBuilders.get("/").buildRequest(null);
        token = jwtTokenService.getToken(req);
        Assert.assertFalse("Could extract token from header", token.isPresent());
    }

    @Test(timeout = 10_000L)
    public void testExpireToken() throws JOSEException, InterruptedException {
        AccessToken token = jwtTokenService.generateToken(getJwtUser());
        Assert.assertNotNull("No token generated", token);

        Assert.assertTrue("Token expired", jwtTokenService.validateToken(token));
        Thread.sleep(jwtTokenService.getExpiration() * 1000 + 100);
        Assert.assertFalse("Token didn't expire", jwtTokenService.validateToken(token));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownAlgorithm() throws Exception {
        Field algorithm = JwtTokenServiceImpl.class.getDeclaredField("algorithm");
        algorithm.setAccessible(true);
        Object oldValue = algorithm.get(jwtTokenService);
        try {
            algorithm.set(jwtTokenService, "Foobar");
            ((InitializingBean) jwtTokenService).afterPropertiesSet();
        } finally {
            algorithm.set(jwtTokenService, oldValue);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHmacNoKey() throws Exception {
        Field algorithm = JwtTokenServiceImpl.class.getDeclaredField("algorithm");
        Field hmacKey = JwtTokenServiceImpl.class.getDeclaredField("hmacKey");
        algorithm.setAccessible(true);
        hmacKey.setAccessible(true);
        Object oldAlgo = algorithm.get(jwtTokenService);
        Object oldHmacKey = hmacKey.get(jwtTokenService);
        try {
            algorithm.set(jwtTokenService, "HS256");
            hmacKey.set(jwtTokenService, "/dev/null");
            ((InitializingBean) jwtTokenService).afterPropertiesSet();
        } finally {
            algorithm.set(jwtTokenService, oldAlgo);
            hmacKey.set(jwtTokenService, oldHmacKey);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEcNoPrivKey() throws Exception {
        Field algorithm = JwtTokenServiceImpl.class.getDeclaredField("algorithm");
        Field privKey = JwtTokenServiceImpl.class.getDeclaredField("privKey");
        algorithm.setAccessible(true);
        privKey.setAccessible(true);
        Object oldAlgo = algorithm.get(jwtTokenService);
        Object oldHmacKey = privKey.get(jwtTokenService);
        try {
            algorithm.set(jwtTokenService, "ES256");
            privKey.set(jwtTokenService, "/dev/null");
            ((InitializingBean) jwtTokenService).afterPropertiesSet();
        } finally {
            algorithm.set(jwtTokenService, oldAlgo);
            privKey.set(jwtTokenService, oldHmacKey);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRsaNoPrivKey() throws Exception {
        Field algorithm = JwtTokenServiceImpl.class.getDeclaredField("algorithm");
        Field privKey = JwtTokenServiceImpl.class.getDeclaredField("privKey");
        algorithm.setAccessible(true);
        privKey.setAccessible(true);
        Object oldAlgo = algorithm.get(jwtTokenService);
        Object oldHmacKey = privKey.get(jwtTokenService);
        try {
            algorithm.set(jwtTokenService, "RS256");
            privKey.set(jwtTokenService, "/dev/null");
            ((InitializingBean) jwtTokenService).afterPropertiesSet();
        } finally {
            algorithm.set(jwtTokenService, oldAlgo);
            privKey.set(jwtTokenService, oldHmacKey);
        }
    }

    @Test
    public void testRefreshTokenLengthTooSmall() throws Exception {
        Field refreshLength = JwtTokenServiceImpl.class.getDeclaredField("refreshLength");
        refreshLength.setAccessible(true);
        Object oldValue = refreshLength.get(jwtTokenService);
        try {
            refreshLength.set(jwtTokenService, -1);
            ((InitializingBean) jwtTokenService).afterPropertiesSet();
        } finally {
            refreshLength.set(jwtTokenService, oldValue);
        }
    }

    @Test
    public void testRefreshTokenLengthTooLarge() throws Exception {
        Field refreshLength = JwtTokenServiceImpl.class.getDeclaredField("refreshLength");
        refreshLength.setAccessible(true);
        Object oldValue = refreshLength.get(jwtTokenService);
        try {
            refreshLength.set(jwtTokenService, Integer.MAX_VALUE);
            ((InitializingBean) jwtTokenService).afterPropertiesSet();
        } finally {
            refreshLength.set(jwtTokenService, oldValue);
        }
    }

    @Test
    public void testGenerateRefreshToken() {
        Assert.assertNotNull("Token should be generated", jwtTokenService.generateRefreshToken("jsmith"));
    }

    @Test
    public void testNullAlgorithm() throws Exception {
        final String token = "eyJhbGciOm51bGx9.eyJzdWIiOiJ4IiwiZXhwIjoyNDk1MTE4NzIwLCJpYXQiOjE0OTUxMTg3MTh9.Nsf-8lD7sCCz5ZH9AErrrYm9SYEXi_MO2z9BA4MOIXE";
        Assert.assertFalse("Accepted NULL algorithm", jwtTokenService.validateToken(token));
    }

    @Test
    public void testUnknownTokenAlgorithm() throws Exception {
        final String token = "eyJhbGciOiJmb29iYXIifQ.eyJzdWIiOiJ4IiwiZXhwIjoyNDk1MTE4NzIwLCJpYXQiOjE0OTUxMTg3MTh9.09UuAR7VTtcmYf8_NzSmAHbEFghFb6igTEx6bgnt7OA";
        Assert.assertFalse("Accepted unknown algorithm", jwtTokenService.validateToken(token));
    }
}
