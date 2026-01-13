/*
 * MIT Licence
 * Copyright (c) 2026 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.ut.service;

import eu.fraho.spring.securityJwt.base.config.RefreshCookieProperties;
import eu.fraho.spring.securityJwt.base.config.RefreshProperties;
import eu.fraho.spring.securityJwt.base.config.TokenCookieProperties;
import eu.fraho.spring.securityJwt.base.config.TokenHeaderProperties;
import eu.fraho.spring.securityJwt.base.config.TokenProperties;
import eu.fraho.spring.securityJwt.base.dto.AccessToken;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import eu.fraho.spring.securityJwt.base.dto.RefreshToken;
import eu.fraho.spring.securityJwt.base.dto.TimeWithPeriod;
import eu.fraho.spring.securityJwt.base.exceptions.FeatureNotConfiguredException;
import eu.fraho.spring.securityJwt.base.it.spring.UserDetailsServiceTestImpl;
import eu.fraho.spring.securityJwt.base.service.JwtTokenService;
import eu.fraho.spring.securityJwt.base.service.JwtTokenServiceImpl;
import eu.fraho.spring.securityJwt.base.service.RefreshTokenStore;
import eu.fraho.spring.securityJwt.base.util.JwtTokens;
import eu.fraho.spring.securityJwt.base.util.MyJwtUser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

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

public abstract class AbstractJwtTokenServiceTest {
    /**
     * private key or hmac secret
     */
    private final File tempKey;
    private final File tempPub;
    protected RefreshTokenStore tokenstoreMock;

    public AbstractJwtTokenServiceTest() throws IOException {
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

    protected TokenProperties getTokenProperties() {
        TokenProperties tokenProperties = new TokenProperties();
        tokenProperties.setHmac(tempKey.toPath());
        tokenProperties.afterPropertiesSet();
        return tokenProperties;
    }

    protected TokenProperties getRsaTokenProperties() {
        TokenProperties tokenProperties = getTokenProperties();
        tokenProperties.setAlgorithm("RS256");
        tokenProperties.setPub(tempPub.toPath());
        writeRsa();
        tokenProperties.afterPropertiesSet();
        return tokenProperties;
    }

    protected RefreshProperties getRefreshProperties() {
        RefreshProperties configuration = new RefreshProperties();
        configuration.setExpiration(new TimeWithPeriod(2, ChronoUnit.SECONDS));
        configuration.afterPropertiesSet();
        return configuration;
    }

    protected TokenCookieProperties getTokenCookieProperties() {
        return new TokenCookieProperties();
    }

    protected TokenHeaderProperties getTokenHeaderProperties() {
        return new TokenHeaderProperties();
    }

    protected RefreshCookieProperties getRefreshCookieProperties() {
        return new RefreshCookieProperties();
    }

    protected JwtTokenService getService() {
        return getService(getTokenProperties(), getRefreshProperties(), getRefreshStore(), new JwtUser(),
                getTokenCookieProperties(), getTokenHeaderProperties(), getRefreshCookieProperties());
    }

    protected JwtTokenService getService(RefreshCookieProperties refreshCookieProperties) {
        return getService(getTokenProperties(), getRefreshProperties(), getRefreshStore(), new JwtUser(),
                getTokenCookieProperties(), getTokenHeaderProperties(), refreshCookieProperties);
    }

    protected JwtTokenService getService(TokenCookieProperties tokenCookieProperties) {
        return getService(getTokenProperties(), getRefreshProperties(), getRefreshStore(), new JwtUser(),
                tokenCookieProperties, getTokenHeaderProperties(), getRefreshCookieProperties());
    }

    protected JwtTokenService getService(TokenProperties tokenProperties) {
        return getService(tokenProperties, getRefreshProperties(), getRefreshStore(), new JwtUser(),
                getTokenCookieProperties(), getTokenHeaderProperties(), getRefreshCookieProperties());
    }

    protected JwtTokenService getService(JwtUser jwtUser) {
        return getService(getTokenProperties(), getRefreshProperties(), getRefreshStore(), jwtUser,
                getTokenCookieProperties(), getTokenHeaderProperties(), getRefreshCookieProperties());
    }

    protected JwtTokenService getService(TokenProperties tokenProperties,
                                         RefreshProperties refreshProperties,
                                         RefreshTokenStore refreshTokenStore,
                                         JwtUser jwtUser,
                                         TokenCookieProperties tokenCookieProperties,
                                         TokenHeaderProperties tokenHeaderProperties,
                                         RefreshCookieProperties refreshCookieProperties) {
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

    private JwtTokenService getNoPrivService() {
        TokenProperties tokenProperties = getRsaTokenProperties();
        tokenProperties.setPriv(null);
        return getService(tokenProperties);
    }

    protected RefreshTokenStore getRefreshStore() {
        tokenstoreMock = Mockito.mock(RefreshTokenStore.class);
        Mockito.when(tokenstoreMock.useToken(Mockito.any())).thenReturn(Optional.empty());

        return tokenstoreMock;
    }

    protected PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    protected UserDetailsService getUserdetailsService() {
        UserDetailsServiceTestImpl userDetailsServiceTest = new UserDetailsServiceTestImpl();
        userDetailsServiceTest.setPasswordEncoder(getPasswordEncoder());
        userDetailsServiceTest.setJwtUser(JwtUser::new);
        return userDetailsServiceTest;
    }

    protected JwtUser getJwtUser() {
        JwtUser user = new JwtUser();
        user.setId(ThreadLocalRandom.current().nextLong(1, 10_000_000));
        user.setUsername("John Snow");
        user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("HOUSE_STARK")));
        return user;
    }

    protected MyJwtUser getMyJwtUser() {
        MyJwtUser user = new MyJwtUser();
        user.setId(13_42L);
        user.setUsername("John Snow");
        user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("HOUSE_STARK")));
        user.setFoobar("this is just a simple custom field for demonstration");
        return user;
    }

    @BeforeEach
    public void beforeTest() {
        tokenstoreMock = null;
    }

    @Test
    public void testParseUser() {
        JwtTokenService service = getService();

        String token = JwtTokens.VALID;
        Optional<JwtUser> oUser = service.parseUser(token);
        Assertions.assertTrue(oUser.isPresent(), "User was not parsed from token");
        JwtUser user = oUser.get();

        Assertions.assertEquals(Long.valueOf(-1), user.getId(), "Parsed user with wrong id");
        Assertions.assertEquals("foo", user.getUsername(), "Parsed user with wrong username");
        Assertions.assertEquals("ROLE_USER", user.getAuthorities().iterator().next().toString(), "Parsed user with wrong role");
    }

    @Test
    public void testParseUserInvalid() {
        JwtTokenService service = getService();

        String tokenInvalidSignature = JwtTokens.INVALID_SIGNATURE;
        Optional<JwtUser> oUser1 = service.parseUser(tokenInvalidSignature);
        Assertions.assertFalse(oUser1.isPresent(), "User was parsed from token");

        String tokenInvalidBody = JwtTokens.INVALID_BODY;
        Optional<JwtUser> oUser2 = service.parseUser(tokenInvalidBody);
        Assertions.assertFalse(oUser2.isPresent(), "User was parsed from token");
    }


    @Test
    public void testValidateToken() {
        JwtTokenService service = getService();

        // regular already handled by testParseUser

        // no iat
        String noIat = JwtTokens.NO_IAT;
        Assertions.assertTrue(service.validateToken(noIat), "AbstractToken is invalid");
        // future iat (false)
        String futureIat = JwtTokens.FUTURE_IAT;
        Assertions.assertFalse(service.validateToken(futureIat), "AbstractToken is invalid");
        // no nbf
        String noNbf = JwtTokens.NO_NBF;
        Assertions.assertTrue(service.validateToken(noNbf), "AbstractToken is invalid");
        // future nbf (false)
        String futureNbf = JwtTokens.FUTURE_NBF;
        Assertions.assertFalse(service.validateToken(futureNbf), "AbstractToken is invalid");
        // no exp (false)
        AccessToken noExp = new AccessToken(JwtTokens.NO_EXP, -1);
        Assertions.assertFalse(service.validateToken(noExp), "AbstractToken is invalid");
    }

    @Test
    public void testValidateTokenInvalid() {
        JwtTokenService service = getService();

        String token = JwtTokens.INVALID_BODY;
        Assertions.assertFalse(service.validateToken(token), "AbstractToken is invalid");
    }

    @Test
    public void testValidateTokenParseException() {
        JwtTokenService service = getService();

        String token = "ö";
        Assertions.assertFalse(service.validateToken(token), "AbstractToken is invalid");
    }

    @Test
    public void testGetToken() {
        JwtTokenService service = getService();

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer foobar", "foobar", null);

        Assertions.assertEquals(Optional.of("foobar"), service.getAccessToken(request), "AbstractToken not extracted from header with Bearer prefix");
        Assertions.assertEquals(Optional.of("foobar"), service.getAccessToken(request), "AbstractToken not extracted from header without Bearer prefix");
        Assertions.assertEquals(Optional.empty(), service.getAccessToken(request), "AbstractToken extracted from header");
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

        Assertions.assertEquals(Optional.of("foobar"), service.getAccessToken(request), "AbstractToken not extracted from header");
        Assertions.assertEquals(Optional.of("foobar"), service.getAccessToken(request), "AbstractToken not extracted from header");
        Assertions.assertEquals(Optional.empty(), service.getAccessToken(request), "AbstractToken extracted from header");
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

        Assertions.assertEquals(Optional.of("foobar"), service.getAccessToken(request), "AbstractToken not extracted from header");
    }

    @Test
    public void testGetRefreshToken() {
        RefreshCookieProperties refreshCookieProperties = new RefreshCookieProperties();
        refreshCookieProperties.setEnabled(true);
        JwtTokenService service = getService(refreshCookieProperties);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getCookies()).thenReturn(new Cookie[]{new Cookie(refreshCookieProperties.getNames()[0], "foobar")}, (Cookie[]) null);

        Assertions.assertEquals(Optional.of("foobar"), service.getRefreshToken(request), "AbstractToken not extracted from cookies");
        Assertions.assertEquals(Optional.empty(), service.getRefreshToken(request), "AbstractToken extracted from header");
    }

    @Test
    public void testGetRefreshTokenDisabled() {
        JwtTokenService service = getService();
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Assertions.assertEquals(Optional.empty(), service.getRefreshToken(request), "AbstractToken extracted from header");
    }

    @Test
    public void testGenerateToken() throws Exception {
        JwtTokenService service = getService();
        JwtUser user = getJwtUser();

        AccessToken token = service.generateToken(user);
        Assertions.assertNotNull(token.getToken(), "No token generated");
    }

    @Test
    public void testGenerateTokenNoPrivateKey() {
        TokenProperties tokenProperties = getRsaTokenProperties();
        JwtTokenService service = getService(tokenProperties);
        JwtUser user = getJwtUser();

        Assertions.assertThrows(FeatureNotConfiguredException.class, () -> service.generateToken(user));
    }

    @Test
    public void testGenerateAndParseTokenCustomize() throws Exception {
        JwtTokenService service = getService(new MyJwtUser());
        MyJwtUser user = getMyJwtUser();

        AccessToken token = service.generateToken(user);
        Assertions.assertNotNull(token.getToken(), "No token generated");

        Optional<MyJwtUser> user1 = service.parseUser(token.getToken());
        Assertions.assertTrue(user1.isPresent(), "User should be parsed");
        Assertions.assertEquals(user.getFoobar(), user1.get().getFoobar(), "Custom claim should be present");
    }

    @Test
    public void testUseRefreshTokenNoPriv() {
        JwtTokenService service = getNoPrivService();

        Assertions.assertThrows(FeatureNotConfiguredException.class, () -> service.useRefreshToken(new RefreshToken("foobar", -1)));

        if (tokenstoreMock != null) {
            Mockito.verifyNoInteractions(tokenstoreMock);
        }
    }

    @Test
    public void testUseRefreshToken() {
        JwtTokenService service = getService();

        RefreshToken token = new RefreshToken("foobar", -1);
        Assertions.assertFalse(service.useRefreshToken(token).isPresent(), "Unknown token used");

        if (tokenstoreMock != null) {
            Mockito.verify(tokenstoreMock).useToken(Mockito.eq(token.getToken()));
        }
    }

    @Test
    public void testGenerateRefreshToken() {
        JwtTokenService service = getService();
        JwtUser user = getJwtUser();
        RefreshToken token1 = service.generateRefreshToken(user);
        Assertions.assertNotNull(token1.getToken(), "No token generated");
        Assertions.assertEquals(getRefreshProperties().getExpiration().toSeconds(), token1.getExpiresIn(), "Wrong expiresIn");

        if (tokenstoreMock != null) {
            Mockito.verify(tokenstoreMock).saveToken(Mockito.eq(user), Mockito.eq(token1.getToken()));
        }

        RefreshToken token2 = service.generateRefreshToken(user);
        Assertions.assertNotNull(token2.getToken(), "No token generated");
        Assertions.assertNotEquals(token1, token2, "No new token generated");
    }
}
