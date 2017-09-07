/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.ut.service;

import eu.fraho.spring.securityJwt.config.CryptConfiguration;
import eu.fraho.spring.securityJwt.config.JwtRefreshConfiguration;
import eu.fraho.spring.securityJwt.config.JwtTokenConfiguration;
import eu.fraho.spring.securityJwt.dto.AccessToken;
import eu.fraho.spring.securityJwt.dto.JwtUser;
import eu.fraho.spring.securityJwt.dto.TimeWithPeriod;
import eu.fraho.spring.securityJwt.exceptions.FeatureNotConfiguredException;
import eu.fraho.spring.securityJwt.it.spring.MockTokenStore;
import eu.fraho.spring.securityJwt.it.spring.UserDetailsServiceTestImpl;
import eu.fraho.spring.securityJwt.password.CryptPasswordEncoder;
import eu.fraho.spring.securityJwt.service.JwtTokenService;
import eu.fraho.spring.securityJwt.service.JwtTokenServiceImpl;
import eu.fraho.spring.securityJwt.service.RefreshTokenStore;
import eu.fraho.spring.securityJwt.util.JwtTokens;
import eu.fraho.spring.securityJwt.util.MyJwtUser;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class TestJwtTokenService {
    /**
     * private key or hmac secret
     */
    private final File tempKey;
    private final File tempPub;

    public TestJwtTokenService() throws IOException {
        super();
        tempKey = File.createTempFile("security-jwt-", ".tmp");
        tempPub = File.createTempFile("security-jwt-", ".tmp");

        try (OutputStream os = new FileOutputStream(tempKey)) {
            os.write("Another stupid dummy value as a constant key".getBytes(StandardCharsets.US_ASCII));
        }
    }

    private void writeRsa() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
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
    }

    @NotNull
    protected JwtTokenConfiguration getTokenConfig() throws Exception {
        JwtTokenConfiguration configuration = new JwtTokenConfiguration();
        configuration.setHmac(tempKey.toPath());
        configuration.afterPropertiesSet();
        return configuration;
    }

    @NotNull
    protected JwtTokenConfiguration getRsaTokenConfig() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        tokenConfiguration.setAlgorithm("RS256");
        tokenConfiguration.setPub(tempPub.toPath());
        writeRsa();
        tokenConfiguration.afterPropertiesSet();
        return tokenConfiguration;
    }

    @NotNull
    protected JwtRefreshConfiguration getRefreshConfig() {
        JwtRefreshConfiguration configuration = new JwtRefreshConfiguration();
        configuration.setExpiration(new TimeWithPeriod(2, TimeUnit.SECONDS));
        configuration.afterPropertiesSet();
        return configuration;
    }

    @NotNull
    protected JwtTokenService getService(@NotNull JwtTokenConfiguration tokenConfiguration,
                                         @NotNull JwtRefreshConfiguration refreshConfiguration,
                                         @NotNull RefreshTokenStore refreshTokenStore,
                                         @NotNull JwtUser jwtUser) {
        JwtTokenServiceImpl tokenService = new JwtTokenServiceImpl(tokenConfiguration, refreshConfiguration, () -> jwtUser);
        tokenService.afterPropertiesSet();
        tokenService.setRefreshTokenStore(refreshTokenStore);
        return tokenService;
    }

    @NotNull
    protected RefreshTokenStore getRefreshStore() {
        MockTokenStore tokenStore = new MockTokenStore();
        tokenStore.afterPropertiesSet();
        return tokenStore;
    }

    @NotNull
    protected CryptConfiguration getCryptConfiguration() {
        return new CryptConfiguration();
    }

    @NotNull
    protected PasswordEncoder getPasswordEncoder() {
        return new CryptPasswordEncoder(getCryptConfiguration());
    }

    @NotNull
    protected UserDetailsService getUserdetailsService() {
        return new UserDetailsServiceTestImpl(getPasswordEncoder(), JwtUser::new);
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

    @Test
    public void testParseUser() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore, new JwtUser());

        String token = JwtTokens.VALID;
        Optional<JwtUser> oUser = service.parseUser(token);
        Assert.assertTrue("User was not parsed from token", oUser.isPresent());
        JwtUser user = oUser.get();

        Assert.assertEquals("Parsed user with wrong id", Long.valueOf(-1), user.getId());
        Assert.assertEquals("Parsed user with wrong username", "foo", user.getUsername());
        Assert.assertEquals("Parsed user with wrong role", "ROLE_USER", user.getAuthorities().iterator().next().toString());
    }

    @Test
    public void testParseUserInvalid() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore, new JwtUser());

        String tokenInvalidSignature = JwtTokens.INVALID_SIGNATURE;
        Optional<JwtUser> oUser1 = service.parseUser(tokenInvalidSignature);
        Assert.assertFalse("User was parsed from token", oUser1.isPresent());

        String tokenInvalidBody = JwtTokens.INVALID_BODY;
        Optional<JwtUser> oUser2 = service.parseUser(tokenInvalidBody);
        Assert.assertFalse("User was parsed from token", oUser2.isPresent());
    }


    @Test
    public void testValidateToken() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore, new JwtUser());

        // regular already handled by testParseUser

        // no iat
        String noIat = JwtTokens.NO_IAT;
        Assert.assertTrue("Token is invalid", service.validateToken(noIat));
        // future iat (false)
        String futureIat = JwtTokens.FUTURE_IAT;
        Assert.assertFalse("Token is invalid", service.validateToken(futureIat));
        // no nbf
        String noNbf = JwtTokens.NO_NBF;
        Assert.assertTrue("Token is invalid", service.validateToken(noNbf));
        // future nbf (false)
        String futureNbf = JwtTokens.FUTURE_NBF;
        Assert.assertFalse("Token is invalid", service.validateToken(futureNbf));
        // no exp (false)
        AccessToken noExp = new AccessToken(JwtTokens.NO_EXP, -1);
        Assert.assertFalse("Token is invalid", service.validateToken(noExp));
    }

    @Test
    public void testValidateTokenInvalid() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore, new JwtUser());

        String token = JwtTokens.INVALID_BODY;
        Assert.assertFalse("Token is invalid", service.validateToken(token));
    }

    @Test
    public void testGetToken() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore, new JwtUser());

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer foobar", "foobar", null);

        Assert.assertEquals("Token not extracted from header", Optional.of("foobar"), service.getToken(request));
        Assert.assertEquals("Token not extracted from header", Optional.of("foobar"), service.getToken(request));
        Assert.assertEquals("Token extracted from header", Optional.empty(), service.getToken(request));
    }

    @Test
    public void testGenerateToken() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore, new JwtUser());
        JwtUser user = getJwtUser();

        AccessToken token = service.generateToken(user);
        Assert.assertNotNull("No token generated", token.getToken());
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testGenerateTokenNoPrivateKey() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getRsaTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore, new JwtUser());
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
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore, new MyJwtUser());
        MyJwtUser user = getMyJwtUser();

        AccessToken token = service.generateToken(user);
        Assert.assertNotNull("No token generated", token.getToken());

        Optional<MyJwtUser> user1 = service.parseUser(token.getToken());
        Assert.assertTrue("User should be parsed", user1.isPresent());
        Assert.assertEquals("Custom claim should be present", user.getFoobar(), user1.get().getFoobar());
    }
}
