/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.tokenService;

import com.nimbusds.jose.JOSEException;
import eu.fraho.spring.securityJwt.AbstractTest;
import eu.fraho.spring.securityJwt.dto.AccessToken;
import eu.fraho.spring.securityJwt.dto.JwtUser;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
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
        withTempField("algorithm", "Foobar", this::reloadTokenService);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHmacNoKey() throws Throwable {
        checkAndCreateOutDirs(OUT_KEY);
        Files.write(Paths.get(OUT_KEY), new byte[0]);

        withTempField("algorithm", "HS256", this::reloadTokenService);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEcEmptyPrivKey() throws Throwable {
        TestJwtServiceCreateTokenEcdsa.beforeClass();
        checkAndCreateOutDirs(TestJwtServiceCreateTokenEcdsa.OUT_PRIV_KEY);
        Files.write(Paths.get(TestJwtServiceCreateTokenEcdsa.OUT_PRIV_KEY), new byte[0]);

        withTempField("algorithm", "ES256", () ->
                withTempField("privKey", Paths.get(TestJwtServiceCreateTokenEcdsa.OUT_PRIV_KEY), this::reloadTokenService));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEcEmptyPubKey() throws Throwable {
        TestJwtServiceCreateTokenEcdsa.beforeClass();
        checkAndCreateOutDirs(TestJwtServiceCreateTokenEcdsa.OUT_PUB_KEY);
        Files.write(Paths.get(TestJwtServiceCreateTokenEcdsa.OUT_PUB_KEY), new byte[0]);

        withTempField("algorithm", "ES256", () ->
                withTempField("pubKey", Paths.get(TestJwtServiceCreateTokenEcdsa.OUT_PUB_KEY), this::reloadTokenService));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRsaEmptyPrivKey() throws Throwable {
        TestJwtServiceCreateTokenRsa.beforeClass();
        checkAndCreateOutDirs(TestJwtServiceCreateTokenRsa.OUT_PRIV_KEY);
        Files.write(Paths.get(TestJwtServiceCreateTokenRsa.OUT_PRIV_KEY), new byte[0]);

        withTempField("algorithm", "RS256", () ->
                withTempField("privKey", Paths.get(TestJwtServiceCreateTokenRsa.OUT_PRIV_KEY), this::reloadTokenService));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRsaEmptyPubKey() throws Throwable {
        TestJwtServiceCreateTokenRsa.beforeClass();
        checkAndCreateOutDirs(TestJwtServiceCreateTokenRsa.OUT_PUB_KEY);
        Files.write(Paths.get(TestJwtServiceCreateTokenRsa.OUT_PUB_KEY), new byte[0]);

        withTempField("algorithm", "RS256", () ->
                withTempField("pubKey", Paths.get(TestJwtServiceCreateTokenRsa.OUT_PUB_KEY), this::reloadTokenService));
    }

    @Test
    public void testRsaNoPubKey() throws Throwable {
        TestJwtServiceCreateTokenRsa.beforeClass();
        Assert.assertTrue("Keyfile should be deleted", new File(TestJwtServiceCreateTokenRsa.OUT_PUB_KEY).delete());

        try {
            withTempField("algorithm", "RS256", () ->
                    withTempField("pubKey", Paths.get(TestJwtServiceCreateTokenRsa.OUT_PUB_KEY), this::reloadTokenService)
            );
        } catch (RuntimeException rex) {
            Assert.assertTrue(NoSuchFileException.class.isInstance(rex.getCause()));
        }
    }

    private void reloadTokenService() {
        try {
            jwtTokenService.afterPropertiesSet();
        } catch (RuntimeException rex) {
            throw rex;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void checkRefreshLengthDefault() {
        reloadTokenService();
        Assert.assertEquals((long) JwtTokenServiceImpl.REFRESH_TOKEN_LEN_DEFAULT, (long) jwtTokenService.getRefreshLength());
    }

    @Test
    public void testRefreshTokenLengthTooSmall() throws Exception {
        withTempField("refreshLength", -1, this::checkRefreshLengthDefault);
    }

    @Test
    public void testRefreshTokenLengthTooLarge() throws Exception {
        withTempField("refreshLength", 10000000, this::checkRefreshLengthDefault);
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
}
