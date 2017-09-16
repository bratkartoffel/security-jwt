/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.ut;

import eu.fraho.spring.securityJwt.JwtAuthenticationTokenFilter;
import eu.fraho.spring.securityJwt.dto.JwtUser;
import eu.fraho.spring.securityJwt.service.JwtTokenService;
import eu.fraho.spring.securityJwt.service.JwtTokenServiceImpl;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class TestJwtAuthenticationTokenFilter {
    @NotNull
    protected JwtTokenService getService() {
        return Mockito.mock(JwtTokenServiceImpl.class);
    }

    @NotNull
    private JwtAuthenticationTokenFilter getNewInstance(JwtTokenService service) {
        return new JwtAuthenticationTokenFilter(service);
    }

    @Before
    public void cleanSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    public void testRegularAuthentication() throws ServletException, IOException {
        JwtTokenService service = getService();
        Mockito.when(service.getAccessToken(Mockito.any())).thenReturn(Optional.of("foobar"));
        Mockito.when(service.parseUser(Mockito.any())).thenReturn(Optional.of(new JwtUser()));

        FilterChain chain = Mockito.mock(FilterChain.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        JwtAuthenticationTokenFilter instance = getNewInstance(service);

        instance.doFilter(request, response, chain);
        Mockito.verify(chain).doFilter(request, response);

        Assert.assertNotNull("Authentication failed", SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void testNoTokenPresent() throws ServletException, IOException {
        JwtTokenService service = getService();
        Mockito.when(service.getAccessToken(Mockito.any())).thenReturn(Optional.empty());

        FilterChain chain = Mockito.mock(FilterChain.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        JwtAuthenticationTokenFilter instance = getNewInstance(service);

        instance.doFilter(request, response, chain);
        Mockito.verify(chain).doFilter(request, response);

        Assert.assertNull("Authentication succeeded", SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void testParseFailed() throws ServletException, IOException {
        JwtTokenService service = getService();
        Mockito.when(service.getAccessToken(Mockito.any())).thenReturn(Optional.of("foobar"));
        Mockito.when(service.parseUser(Mockito.any())).thenReturn(Optional.empty());

        FilterChain chain = Mockito.mock(FilterChain.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        JwtAuthenticationTokenFilter instance = getNewInstance(service);

        instance.doFilter(request, response, chain);
        Mockito.verify(chain).doFilter(request, response);

        Assert.assertNull("Authentication succeeded", SecurityContextHolder.getContext().getAuthentication());
    }
}
