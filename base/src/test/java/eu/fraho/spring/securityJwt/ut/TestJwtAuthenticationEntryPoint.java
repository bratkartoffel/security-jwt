/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.ut;

import eu.fraho.spring.securityJwt.JwtAuthenticationEntryPoint;
import eu.fraho.spring.securityJwt.config.JwtRefreshCookieConfiguration;
import eu.fraho.spring.securityJwt.service.JwtTokenService;
import eu.fraho.spring.securityJwt.service.JwtTokenServiceImpl;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class TestJwtAuthenticationEntryPoint {
    @NotNull
    protected JwtRefreshCookieConfiguration getRefreshCookieConfig() {
        JwtRefreshCookieConfiguration cookieConfiguration = new JwtRefreshCookieConfiguration();
        cookieConfiguration.setEnabled(true);
        return cookieConfiguration;
    }

    @NotNull
    protected JwtTokenService getService() {
        JwtTokenServiceImpl tokenService = Mockito.mock(JwtTokenServiceImpl.class);
        Mockito.when(tokenService.getRefreshToken(Mockito.any())).thenReturn(Optional.of("foobar"));
        return tokenService;
    }

    @NotNull
    private JwtAuthenticationEntryPoint getNewInstance(@NotNull final JwtRefreshCookieConfiguration cookieConfiguration) {
        return new JwtAuthenticationEntryPoint(cookieConfiguration, getService());
    }

    @NotNull
    private JwtAuthenticationEntryPoint getNewInstance() {
        return new JwtAuthenticationEntryPoint(getRefreshCookieConfig(), getService());
    }

    @Test
    public void testRedirectNoDomain() throws IOException {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        JwtAuthenticationEntryPoint instance = getNewInstance();

        instance.commence(request, response, null);

        Mockito.verify(response).sendRedirect("/auth/refreshCookie");
    }

    @Test
    public void testResponseNoCookies() throws IOException {
        JwtRefreshCookieConfiguration cookieConfiguration = getRefreshCookieConfig();
        cookieConfiguration.setEnabled(false);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        JwtAuthenticationEntryPoint instance = getNewInstance(cookieConfiguration);

        instance.commence(request, response, null);

        Mockito.verify(response).sendError(401, "Unauthorized");
    }

    @Test
    public void testRedirectWithDomainOnly() throws IOException {
        JwtRefreshCookieConfiguration cookieConfiguration = getRefreshCookieConfig();
        cookieConfiguration.setDomain("foobar.com");
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        JwtAuthenticationEntryPoint instance = getNewInstance(cookieConfiguration);

        instance.commence(request, response, null);

        Mockito.verify(response).sendRedirect("https://foobar.com/auth/refreshCookie");
    }

    @Test
    public void testRedirectWithDomainNoSecure() throws IOException {
        JwtRefreshCookieConfiguration cookieConfiguration = getRefreshCookieConfig();
        cookieConfiguration.setDomain("foobar.com");
        cookieConfiguration.setSecure(false);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        JwtAuthenticationEntryPoint instance = getNewInstance(cookieConfiguration);

        instance.commence(request, response, null);

        Mockito.verify(response).sendRedirect("http://foobar.com/auth/refreshCookie");
    }

    @Test
    public void testRedirectWithDomainAndPort() throws IOException {
        JwtRefreshCookieConfiguration cookieConfiguration = getRefreshCookieConfig();
        cookieConfiguration.setDomain("foobar.com");
        cookieConfiguration.setPort(42);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        JwtAuthenticationEntryPoint instance = getNewInstance(cookieConfiguration);

        instance.commence(request, response, null);

        Mockito.verify(response).sendRedirect("https://foobar.com:42/auth/refreshCookie");
    }

    @Test
    public void testRedirectWithInvalidUri() throws IOException {
        JwtRefreshCookieConfiguration cookieConfiguration = getRefreshCookieConfig();
        cookieConfiguration.setDomain("foo&bar.com");
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        JwtAuthenticationEntryPoint instance = getNewInstance(cookieConfiguration);

        instance.commence(request, response, null);

        Mockito.verify(response).sendRedirect("/auth/refreshCookie");
    }
}
