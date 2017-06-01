package eu.fraho.spring.securityJwt.tokenService;

import eu.fraho.spring.securityJwt.AbstractTest;
import eu.fraho.spring.securityJwt.dto.RefreshToken;
import eu.fraho.spring.securityJwt.exceptions.FeatureNotConfiguredException;
import eu.fraho.spring.securityJwt.service.JwtTokenService;
import eu.fraho.spring.securityJwt.spring.TestApiApplication;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@Getter
@Setter(AccessLevel.NONE)
@Slf4j
@SpringBootTest(properties = "spring.config.location=classpath:test-refresh-disabled.yaml",
        classes = TestApiApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TestJwtServiceRefreshDisabled extends AbstractTest {
    @Autowired
    protected JwtTokenService jwtTokenService = null;

    @BeforeClass
    public static void beforeClass() throws IOException {
        AbstractTest.beforeHmacClass();
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testGenerateRefreshToken() {
        jwtTokenService.generateRefreshToken("foobar");
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testUseRefreshToken() {
        Assert.assertNull(jwtTokenService.useRefreshToken("bar", "foo"));
        jwtTokenService.useRefreshToken("foo", "baz");
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testListRefreshTokens() {
        jwtTokenService.listRefreshTokens("foobar");
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testListRefreshTokensAll() {
        jwtTokenService.listRefreshTokens();
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testClearRefreshTokens() {
        jwtTokenService.clearTokens();
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testRevokeByName() {
        jwtTokenService.revokeRefreshTokens("john");
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testRevokeByNameAndDevice() {
        jwtTokenService.revokeRefreshToken("john", "mobile");
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testRevokeByNameAndToken() {
        jwtTokenService.revokeRefreshToken("john", new RefreshToken("foobar", 1, "none"));
    }

}
