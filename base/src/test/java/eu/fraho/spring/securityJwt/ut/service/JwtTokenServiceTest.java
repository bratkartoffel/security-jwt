/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.ut.service;

import eu.fraho.spring.securityJwt.config.*;
import eu.fraho.spring.securityJwt.dto.AccessToken;
import eu.fraho.spring.securityJwt.dto.JwtUser;
import eu.fraho.spring.securityJwt.dto.RefreshToken;
import eu.fraho.spring.securityJwt.dto.TimeWithPeriod;
import eu.fraho.spring.securityJwt.exceptions.FeatureNotConfiguredException;
import eu.fraho.spring.securityJwt.it.spring.UserDetailsServiceTestImpl;
import eu.fraho.spring.securityJwt.service.JwtTokenService;
import eu.fraho.spring.securityJwt.service.JwtTokenServiceImpl;
import eu.fraho.spring.securityJwt.service.RefreshTokenStore;
import eu.fraho.spring.securityJwt.util.JwtTokens;
import eu.fraho.spring.securityJwt.util.MyJwtUser;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class JwtTokenServiceTest {
    /**
     * private key or hmac secret
     */
    private final File tempKey;
    private final File tempPub;
    protected RefreshTokenStore tokenstoreMock;

    public JwtTokenServiceTest() throws IOException {
        super();
        tempKey = File.createTempFile("security-jwt-", ".tmp");
        tempPub = File.createTempFile("security-jwt-", ".tmp");

        try (OutputStream os = new FileOutputStream(tempKey)) {
            os.write("Another stupid dummy value as a constant key".getBytes(StandardCharsets.US_ASCII));
        }
    }

    private void writeRsa() {
        try {
            // initialize generator
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(512);

            // generate the key pair
            KeyPair keyPair = keyPairGenerator.genKeyPair();

            // create KeyFactory and RSA Keys Specs
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(keyPair.getPublic().getEncoded()));

            // write the keys
            Files.write(tempPub.toPath(), publicKey.getEncoded());
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to write RSA keys", ex);
        }
    }

    @NotNull
    protected TokenProperties getTokenProperties() {
        TokenProperties tokenProperties = new TokenProperties();
        tokenProperties.setHmac(tempKey.toPath());
        tokenProperties.afterPropertiesSet();
        return tokenProperties;
    }

    @NotNull
    protected TokenProperties getRsaTokenProperties() {
        TokenProperties tokenProperties = getTokenProperties();
        tokenProperties.setAlgorithm("RS256");
        tokenProperties.setPub(tempPub.toPath());
        writeRsa();
        tokenProperties.afterPropertiesSet();
        return tokenProperties;
    }

    @NotNull
    protected RefreshProperties getRefreshProperties() {
        RefreshProperties configuration = new RefreshProperties();
        configuration.setExpiration(new TimeWithPeriod(2, ChronoUnit.SECONDS));
        configuration.afterPropertiesSet();
        return configuration;
    }

    @NotNull
    protected TokenCookieProperties getTokenCookieProperties() {
        return new TokenCookieProperties();
    }

    @NotNull
    protected TokenHeaderProperties getTokenHeaderProperties() {
        return new TokenHeaderProperties();
    }

    @NotNull
    protected RefreshCookieProperties getRefreshCookieProperties() {
        return new RefreshCookieProperties();
    }

    @NotNull
    protected JwtTokenService getService() {
        return getService(getTokenProperties(), getRefreshProperties(), getRefreshStore(), new JwtUser(),
                getTokenCookieProperties(), getTokenHeaderProperties(), getRefreshCookieProperties());
    }

    @NotNull
    protected JwtTokenService getService(@NotNull RefreshCookieProperties refreshCookieProperties) {
        return getService(getTokenProperties(), getRefreshProperties(), getRefreshStore(), new JwtUser(),
                getTokenCookieProperties(), getTokenHeaderProperties(), refreshCookieProperties);
    }

    @NotNull
    protected JwtTokenService getService(@NotNull TokenCookieProperties tokenCookieProperties) {
        return getService(getTokenProperties(), getRefreshProperties(), getRefreshStore(), new JwtUser(),
                tokenCookieProperties, getTokenHeaderProperties(), getRefreshCookieProperties());
    }

    @NotNull
    protected JwtTokenService getService(@NotNull TokenProperties tokenProperties) {
        return getService(tokenProperties, getRefreshProperties(), getRefreshStore(), new JwtUser(),
                getTokenCookieProperties(), getTokenHeaderProperties(), getRefreshCookieProperties());
    }

    @NotNull
    protected JwtTokenService getService(@NotNull JwtUser jwtUser) {
        return getService(getTokenProperties(), getRefreshProperties(), getRefreshStore(), jwtUser,
                getTokenCookieProperties(), getTokenHeaderProperties(), getRefreshCookieProperties());
    }

    @NotNull
    protected JwtTokenService getService(@NotNull TokenProperties tokenProperties,
                                         @NotNull RefreshProperties refreshProperties,
                                         @NotNull RefreshTokenStore refreshTokenStore,
                                         @NotNull JwtUser jwtUser,
                                         @NotNull TokenCookieProperties tokenCookieProperties,
                                         @NotNull TokenHeaderProperties tokenHeaderProperties,
                                         @NotNull RefreshCookieProperties refreshCookieProperties) {
        JwtTokenServiceImpl tokenService = new JwtTokenServiceImpl();
        tokenService.setTokenProperties(tokenProperties);
        tokenService.setRefreshProperties(refreshProperties);
        tokenService.setTokenCookieProperties(tokenCookieProperties);
        tokenService.setTokenHeaderProperties(tokenHeaderProperties);
        tokenService.setRefreshCookieProperties(refreshCookieProperties);
        tokenService.setJwtUser(() -> jwtUser);
        tokenService.setTokenCookieProperties(tokenCookieProperties);
        tokenService.afterPropertiesSet();
        tokenService.setRefreshTokenStore(refreshTokenStore);
        return tokenService;
    }

    @NotNull
    private JwtTokenService getNoPrivService() {
        TokenProperties tokenProperties = getRsaTokenProperties();
        tokenProperties.setPriv(null);
        return getService(tokenProperties);
    }

    @NotNull
    protected RefreshTokenStore getRefreshStore() {
        tokenstoreMock = Mockito.mock(RefreshTokenStore.class);
        Mockito.when(tokenstoreMock.useToken(Mockito.any())).thenReturn(Optional.empty());

        return tokenstoreMock;
    }

    @NotNull
    protected PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @NotNull
    protected UserDetailsService getUserdetailsService() {
        UserDetailsServiceTestImpl userDetailsServiceTest = new UserDetailsServiceTestImpl();
        userDetailsServiceTest.setPasswordEncoder(getPasswordEncoder());
        userDetailsServiceTest.setJwtUser(JwtUser::new);
        return userDetailsServiceTest;
    }

    @NotNull
    protected JwtUser getJwtUser() {
        JwtUser user = new JwtUser();
        user.setId(ThreadLocalRandom.current().nextLong());
        user.setUsername("John Snow");
        user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("HOUSE_STARK")));
        return user;
    }

    @NotNull
    protected MyJwtUser getMyJwtUser() {
        MyJwtUser user = new MyJwtUser();
        user.setId(13_42L);
        user.setUsername("John Snow");
        user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("HOUSE_STARK")));
        user.setFoobar("this is just a simple custom field for demonstration");
        return user;
    }

    @Before
    public void beforeTest() {
        tokenstoreMock = null;
    }

    @Test
    public void testParseUser() {
        JwtTokenService service = getService();

        String token = JwtTokens.VALID;
        Optional<JwtUser> oUser = service.parseUser(token);
        Assert.assertTrue("User was not parsed from token", oUser.isPresent());
        JwtUser user = oUser.get();

        Assert.assertEquals("Parsed user with wrong id", Long.valueOf(-1), user.getId());
        Assert.assertEquals("Parsed user with wrong username", "foo", user.getUsername());
        Assert.assertEquals("Parsed user with wrong role", "ROLE_USER", user.getAuthorities().iterator().next().toString());
    }

    @Test
    public void testParseUserInvalid() {
        JwtTokenService service = getService();

        String tokenInvalidSignature = JwtTokens.INVALID_SIGNATURE;
        Optional<JwtUser> oUser1 = service.parseUser(tokenInvalidSignature);
        Assert.assertFalse("User was parsed from token", oUser1.isPresent());

        String tokenInvalidBody = JwtTokens.INVALID_BODY;
        Optional<JwtUser> oUser2 = service.parseUser(tokenInvalidBody);
        Assert.assertFalse("User was parsed from token", oUser2.isPresent());
    }


    @Test
    public void testValidateToken() {
        JwtTokenService service = getService();

        // regular already handled by testParseUser

        // no iat
        String noIat = JwtTokens.NO_IAT;
        Assert.assertTrue("AbstractToken is invalid", service.validateToken(noIat));
        // future iat (false)
        String futureIat = JwtTokens.FUTURE_IAT;
        Assert.assertFalse("AbstractToken is invalid", service.validateToken(futureIat));
        // no nbf
        String noNbf = JwtTokens.NO_NBF;
        Assert.assertTrue("AbstractToken is invalid", service.validateToken(noNbf));
        // future nbf (false)
        String futureNbf = JwtTokens.FUTURE_NBF;
        Assert.assertFalse("AbstractToken is invalid", service.validateToken(futureNbf));
        // no exp (false)
        AccessToken noExp = new AccessToken(JwtTokens.NO_EXP, -1);
        Assert.assertFalse("AbstractToken is invalid", service.validateToken(noExp));
    }

    @Test
    public void testValidateTokenInvalid() {
        JwtTokenService service = getService();

        String token = JwtTokens.INVALID_BODY;
        Assert.assertFalse("AbstractToken is invalid", service.validateToken(token));
    }

    @Test
    public void testValidateTokenParseException() {
        JwtTokenService service = getService();

        String token = "ö";
        Assert.assertFalse("AbstractToken is invalid", service.validateToken(token));
    }

    @Test
    public void testGetToken() {
        JwtTokenService service = getService();

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer foobar", "foobar", null);

        Assert.assertEquals("AbstractToken not extracted from header", Optional.of("foobar"), service.getAccessToken(request));
        Assert.assertEquals("AbstractToken not extracted from header", Optional.of("foobar"), service.getAccessToken(request));
        Assert.assertEquals("AbstractToken extracted from header", Optional.empty(), service.getAccessToken(request));
    }

    @Test
    public void testGetTokenCookie() {
        TokenCookieProperties tokenCookieProperties = new TokenCookieProperties();
        tokenCookieProperties.setEnabled(true);
        JwtTokenService service = getService(tokenCookieProperties);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getCookies()).thenReturn(
                new Cookie[]{
                        new Cookie("foo", "bar"),
                        new Cookie(tokenCookieProperties.getNames()[0], "foobar")
                },
                new Cookie[]{
                        new Cookie(tokenCookieProperties.getNames()[1], "foobar")
                },
                new Cookie[0]);

        Assert.assertEquals("AbstractToken not extracted from header", Optional.of("foobar"), service.getAccessToken(request));
        Assert.assertEquals("AbstractToken not extracted from header", Optional.of("foobar"), service.getAccessToken(request));
        Assert.assertEquals("AbstractToken extracted from header", Optional.empty(), service.getAccessToken(request));
    }

    @Test
    public void testGetTokenWhenBothThenHeaderPrimary() {
        TokenCookieProperties tokenCookieProperties = new TokenCookieProperties();
        tokenCookieProperties.setEnabled(true);
        JwtTokenService service = getService(tokenCookieProperties);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer foobar");
        Mockito.when(request.getCookies()).thenReturn(
                new Cookie[]{
                        new Cookie(tokenCookieProperties.getNames()[0], "baz")
                });

        Assert.assertEquals("AbstractToken not extracted from header", Optional.of("foobar"), service.getAccessToken(request));
    }

    @Test
    public void testGetRefreshToken() {
        RefreshCookieProperties refreshCookieProperties = new RefreshCookieProperties();
        refreshCookieProperties.setEnabled(true);
        JwtTokenService service = getService(refreshCookieProperties);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getCookies()).thenReturn(new Cookie[]{new Cookie(refreshCookieProperties.getNames()[0], "foobar")}, (Cookie[]) null);

        Assert.assertEquals("AbstractToken not extracted from cookies", Optional.of("foobar"), service.getRefreshToken(request));
        Assert.assertEquals("AbstractToken extracted from header", Optional.empty(), service.getRefreshToken(request));
    }

    @Test
    public void testGetRefreshTokenDisabled() {
        JwtTokenService service = getService();
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Assert.assertEquals("AbstractToken extracted from header", Optional.empty(), service.getRefreshToken(request));
    }

    @Test
    public void testGenerateToken() throws Exception {
        JwtTokenService service = getService();
        JwtUser user = getJwtUser();

        AccessToken token = service.generateToken(user);
        Assert.assertNotNull("No token generated", token.getToken());
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testGenerateTokenNoPrivateKey() throws Exception {
        TokenProperties tokenProperties = getRsaTokenProperties();
        JwtTokenService service = getService(tokenProperties);
        JwtUser user = getJwtUser();

        try {
            service.generateToken(user);
        } catch (FeatureNotConfiguredException fnce) {
            Assert.assertEquals("Wrong error message", "Access token signing is not enabled.", fnce.getMessage());
            throw fnce;
        }
    }

    @Test
    public void testGenerateAndParseTokenCustomize() throws Exception {
        JwtTokenService service = getService(new MyJwtUser());
        MyJwtUser user = getMyJwtUser();

        AccessToken token = service.generateToken(user);
        Assert.assertNotNull("No token generated", token.getToken());

        Optional<MyJwtUser> user1 = service.parseUser(token.getToken());
        Assert.assertTrue("User should be parsed", user1.isPresent());
        Assert.assertEquals("Custom claim should be present", user.getFoobar(), user1.get().getFoobar());
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testUseRefreshTokenNoPriv() {
        JwtTokenService service = getNoPrivService();

        try {
            service.useRefreshToken(new RefreshToken("foobar", -1));
        } catch (FeatureNotConfiguredException fnce) {
            Assert.assertEquals("Wrong error message", "Access token signing is not enabled.", fnce.getMessage());
            throw fnce;
        }

        if (tokenstoreMock != null) {
            Mockito.verifyZeroInteractions(tokenstoreMock);
        }
    }

    @Test
    public void testUseRefreshToken() {
        JwtTokenService service = getService();

        RefreshToken token = new RefreshToken("foobar", -1);
        Assert.assertFalse("Unknown token used", service.useRefreshToken(token).isPresent());

        if (tokenstoreMock != null) {
            Mockito.verify(tokenstoreMock).useToken(Mockito.eq(token.getToken()));
        }
    }

    @Test
    public void testGenerateRefreshToken() {
        JwtTokenService service = getService();
        JwtUser user = getJwtUser();
        RefreshToken token1 = service.generateRefreshToken(user);
        Assert.assertNotNull("No token generated", token1.getToken());
        Assert.assertEquals("Wrong expiresIn", getRefreshProperties().getExpiration().toSeconds(), token1.getExpiresIn());

        if (tokenstoreMock != null) {
            Mockito.verify(tokenstoreMock).saveToken(Mockito.eq(user), Mockito.eq(token1.getToken()));
        }

        RefreshToken token2 = service.generateRefreshToken(user);
        Assert.assertNotNull("No token generated", token2.getToken());
        Assert.assertNotEquals("No new token generated", token1, token2);
    }
}
