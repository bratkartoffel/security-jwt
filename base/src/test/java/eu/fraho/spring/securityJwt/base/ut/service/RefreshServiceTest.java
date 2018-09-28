/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.ut.service;

import com.nimbusds.jose.JOSEException;
import eu.fraho.spring.securityJwt.base.dto.*;
import eu.fraho.spring.securityJwt.base.service.JwtTokenService;
import eu.fraho.spring.securityJwt.base.service.RefreshService;
import eu.fraho.spring.securityJwt.base.service.RefreshServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.Optional;

public class RefreshServiceTest {
    protected JwtTokenService getTokenService() {
        JwtTokenService service = Mockito.mock(JwtTokenService.class);
        Mockito.when(service.isRefreshTokenSupported()).thenReturn(Boolean.TRUE);
        Mockito.when(service.useRefreshToken("valid_active")).thenReturn(Optional.of(getJwtUser(true)));
        Mockito.when(service.useRefreshToken("valid_inactive")).thenReturn(Optional.of(getJwtUser(false)));
        Mockito.when(service.useRefreshToken("invalid")).thenReturn(Optional.empty());
        try {
            Mockito.when(service.generateToken(Mockito.any(JwtUser.class))).thenReturn(AccessToken.builder().token("foo").build());
        } catch (JOSEException je) {
            // cannot happen as we work on a mock
        }
        Mockito.when(service.generateRefreshToken(Mockito.any(JwtUser.class))).thenReturn(RefreshToken.builder().token("bar").build());
        return service;
    }

    protected JwtUser getJwtUser(boolean apiAccessAllowed) {
        JwtUser user = new JwtUser();
        user.setId(42L);
        user.setUsername("John Snow");
        user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("HOUSE_STARK")));
        user.setApiAccessAllowed(apiAccessAllowed);
        return user;
    }

    protected RefreshService getNewInstance() {
        RefreshServiceImpl service = new RefreshServiceImpl();
        service.setJwtTokenService(getTokenService());
        return service;
    }

    @Test(expected = BadCredentialsException.class)
    public void testNullToken() {
        RefreshService instance = getNewInstance();
        instance.checkRefresh(null);
    }

    @Test(expected = BadCredentialsException.class)
    public void testValidTokenInactiveUser() {
        RefreshService instance = getNewInstance();
        instance.checkRefresh("valid_inactive");
    }

    @Test(expected = BadCredentialsException.class)
    public void testInvalidToken() {
        RefreshService instance = getNewInstance();
        instance.checkRefresh("invalid");
    }

    @Test
    public void testValidToken() {
        RefreshService instance = getNewInstance();
        AuthenticationResponse response = instance.checkRefresh("valid_active");

        Assert.assertEquals("foo", response.getAccessToken().getToken());
        Assert.assertEquals("bar", Optional.ofNullable(response.getRefreshToken()).map(AbstractToken::getToken).orElse(null));
    }
}