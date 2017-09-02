/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.ut.service;

import eu.fraho.spring.securityJwt.config.JwtRefreshConfiguration;
import eu.fraho.spring.securityJwt.config.JwtTokenConfiguration;
import eu.fraho.spring.securityJwt.dto.AccessToken;
import eu.fraho.spring.securityJwt.dto.JwtUser;
import eu.fraho.spring.securityJwt.dto.TimeWithPeriod;
import eu.fraho.spring.securityJwt.exceptions.FeatureNotConfiguredException;
import eu.fraho.spring.securityJwt.it.spring.MockTokenStore;
import eu.fraho.spring.securityJwt.service.JwtTokenService;
import eu.fraho.spring.securityJwt.service.JwtTokenServiceImpl;
import eu.fraho.spring.securityJwt.service.RefreshTokenStore;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Collections;
import java.util.Optional;
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

    @NotNull
    public static String loadToken(@NotNull String name) throws IOException, URISyntaxException {
        return new String(
                Files.readAllBytes(Paths.get(TestJwtTokenService.class.getResource(name).toURI())),
                StandardCharsets.US_ASCII);
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
                                         @NotNull RefreshTokenStore refreshTokenStore) {
        JwtTokenServiceImpl tokenService = new JwtTokenServiceImpl(tokenConfiguration, refreshConfiguration);
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
    protected JwtUser getJwtUser() {
        JwtUser user = new JwtUser();
        user.setId(13_42L);
        user.setUsername("John Snow");
        user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("HOUSE_STARK")));
        return user;
    }

    @Test
    public void testParseUser() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore);

        String token = loadToken("/token-valid.txt");
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
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore);

        String tokenInvalidSignature = loadToken("/token-invalid-signature.txt");
        Optional<JwtUser> oUser1 = service.parseUser(tokenInvalidSignature);
        Assert.assertFalse("User was parsed from token", oUser1.isPresent());

        String tokenInvalidBody = loadToken("/token-invalid-body.txt");
        Optional<JwtUser> oUser2 = service.parseUser(tokenInvalidBody);
        Assert.assertFalse("User was parsed from token", oUser2.isPresent());
    }


    @Test
    public void testValidateToken() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore);

        // regular already handled by testParseUser

        // no iat
        String noIat = loadToken("/token-no-iat.txt");
        Assert.assertTrue("Token is invalid", service.validateToken(noIat));
        // future iat (false)
        String futureIat = loadToken("/token-future-iat.txt");
        Assert.assertFalse("Token is invalid", service.validateToken(futureIat));
        // no nbf
        String noNbf = loadToken("/token-no-nbf.txt");
        Assert.assertTrue("Token is invalid", service.validateToken(noNbf));
        // future nbf (false)
        String futureNbf = loadToken("/token-future-nbf.txt");
        Assert.assertFalse("Token is invalid", service.validateToken(futureNbf));
        // no exp (false)
        AccessToken noExp = new AccessToken(loadToken("/token-no-exp.txt"), -1);
        Assert.assertFalse("Token is invalid", service.validateToken(noExp));
    }

    @Test
    public void testValidateTokenInvalid() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore);

        String token = loadToken("/token-invalid-body.txt");
        Assert.assertFalse("Token is invalid", service.validateToken(token));
    }

    @Test
    public void testGetToken() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore);

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
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore);
        JwtUser user = getJwtUser();

        AccessToken token = service.generateToken(user);
        Assert.assertNotNull("No token generated", token.getToken());
    }

    @Test(expected = FeatureNotConfiguredException.class)
    public void testGenerateTokenNoPrivateKey() throws Exception {
        JwtTokenConfiguration tokenConfiguration = getRsaTokenConfig();
        JwtRefreshConfiguration refreshConfiguration = getRefreshConfig();
        RefreshTokenStore refreshTokenStore = getRefreshStore();
        JwtTokenService service = getService(tokenConfiguration, refreshConfiguration, refreshTokenStore);
        JwtUser user = getJwtUser();

        try {
            service.generateToken(user);
        } catch (FeatureNotConfiguredException fnce) {
            Assert.assertEquals("Wrong error message", "Access token signing is not enabled.", fnce.getMessage());
            throw fnce;
        }
    }
}
