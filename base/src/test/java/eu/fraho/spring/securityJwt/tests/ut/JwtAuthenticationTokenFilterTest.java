/*
 * MIT Licence
 * Copyright (c) 2020 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.tests.ut;

import eu.fraho.spring.securityJwt.base.JwtAuthenticationTokenFilter;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import eu.fraho.spring.securityJwt.base.service.JwtTokenService;
import eu.fraho.spring.securityJwt.base.service.JwtTokenServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class JwtAuthenticationTokenFilterTest {
    protected JwtTokenService getService() {
        return Mockito.mock(JwtTokenServiceImpl.class);
    }

    private JwtAuthenticationTokenFilter getNewInstance(JwtTokenService jwtTokenService) {
        JwtAuthenticationTokenFilter filter = new JwtAuthenticationTokenFilter();
        filter.setJwtTokenService(jwtTokenService);
        return filter;
    }

    @BeforeEach
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

        Assertions.assertNotNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication failed");
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

        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication succeeded");
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

        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication succeeded");
    }
}
