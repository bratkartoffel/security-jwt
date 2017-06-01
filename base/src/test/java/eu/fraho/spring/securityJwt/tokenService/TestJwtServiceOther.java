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
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
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
    public void testUnknownAlgorithm() throws Throwable {
        withTempAlorithm("Foobar", this::reloadTokenService);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHmacNoKey() throws Throwable {
        checkAndCreateOutDirs(OUT_KEY);
        Files.write(Paths.get(OUT_KEY), new byte[0]);

        withTempAlorithm("HS256", this::reloadTokenService);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEcEmptyPrivKey() throws Throwable {
        TestJwtServiceCreateTokenEcdsa.beforeClass();
        checkAndCreateOutDirs(TestJwtServiceCreateTokenEcdsa.OUT_PRIV_KEY);
        Files.write(Paths.get(TestJwtServiceCreateTokenEcdsa.OUT_PRIV_KEY), new byte[0]);

        withTempAlorithm("ES256", () -> withTempField("privKey", Paths.get(TestJwtServiceCreateTokenEcdsa.OUT_PRIV_KEY), this::reloadTokenService));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEcEmptyPubKey() throws Throwable {
        TestJwtServiceCreateTokenEcdsa.beforeClass();
        checkAndCreateOutDirs(TestJwtServiceCreateTokenEcdsa.OUT_PUB_KEY);
        Files.write(Paths.get(TestJwtServiceCreateTokenEcdsa.OUT_PUB_KEY), new byte[0]);

        withTempAlorithm("ES256", () -> withTempField("pubKey", Paths.get(TestJwtServiceCreateTokenEcdsa.OUT_PUB_KEY), this::reloadTokenService));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRsaEmptyPrivKey() throws Throwable {
        TestJwtServiceCreateTokenRsa.beforeClass();
        checkAndCreateOutDirs(TestJwtServiceCreateTokenRsa.OUT_PRIV_KEY);
        Files.write(Paths.get(TestJwtServiceCreateTokenRsa.OUT_PRIV_KEY), new byte[0]);

        withTempAlorithm("RS256", () -> withTempField("privKey", Paths.get(TestJwtServiceCreateTokenRsa.OUT_PRIV_KEY), this::reloadTokenService));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRsaEmptyPubKey() throws Throwable {
        TestJwtServiceCreateTokenRsa.beforeClass();
        checkAndCreateOutDirs(TestJwtServiceCreateTokenRsa.OUT_PUB_KEY);
        Files.write(Paths.get(TestJwtServiceCreateTokenRsa.OUT_PUB_KEY), new byte[0]);

        withTempAlorithm("RS256", () -> withTempField("pubKey", Paths.get(TestJwtServiceCreateTokenRsa.OUT_PUB_KEY), this::reloadTokenService));
    }

    @Test(expected = NoSuchFileException.class)
    public void testRsaNoPubKey() throws Throwable {
        TestJwtServiceCreateTokenRsa.beforeClass();
        Assert.assertTrue("Keyfile should be deleted", new File(TestJwtServiceCreateTokenRsa.OUT_PUB_KEY).delete());

        withTempAlorithm("RS256", () -> withTempField("pubKey", Paths.get(TestJwtServiceCreateTokenRsa.OUT_PUB_KEY), this::reloadTokenService));
    }

    private void reloadTokenService() {
        try {
            ((InitializingBean) jwtTokenService).afterPropertiesSet();
        } catch (Exception e) {
            throw new IllegalStateException(e);
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

    @Test
    public void testParseUserGarbageToken() throws Exception {
        final String token = "y51bGx9.eyJzdWIiTh9.Nsf-8lD724MOIXE";
        Assert.assertFalse("No user should be returned", jwtTokenService.parseUser(token).isPresent());
    }


    private void withTempField(String fieldname, Object value, Runnable callback) {
        try {
            final Field field = JwtTokenServiceImpl.class.getDeclaredField(fieldname);
            field.setAccessible(true);
            Object oldValue = field.get(jwtTokenService);
            try {
                field.set(jwtTokenService, value);
                callback.run();
            } finally {
                field.set(jwtTokenService, oldValue);
                callback.run();
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void withTempAlorithm(Object value, Runnable callback) throws Throwable {
        try {
            withTempField("algorithm", value, callback);
        } catch (RuntimeException rex) {
            Throwable tmp = rex;
            while (tmp.getCause() != null) tmp = tmp.getCause();
            throw tmp;
        }
    }
}
