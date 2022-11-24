/*
 * MIT Licence
 * Copyright (c) 2022 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.tests.ut;

import eu.fraho.spring.securityJwt.base.JwtAuthenticationTokenFilter;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import eu.fraho.spring.securityJwt.base.service.JwtTokenService;
import eu.fraho.spring.securityJwt.base.service.JwtTokenServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.lang.reflect.Method;
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
    public void testRegularAuthentication() throws ServletException, IOException, ReflectiveOperationException {
        JwtTokenService service = getService();
        Mockito.when(service.getAccessToken(Mockito.any(HttpServletRequest.class))).thenReturn(Optional.of("foobar"));
        Mockito.when(service.parseUser(Mockito.any())).thenReturn(Optional.of(new JwtUser()));

        FilterChain chain = Mockito.mock(FilterChain.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        JwtAuthenticationTokenFilter instance = getNewInstance(service);

        Method method = JwtAuthenticationTokenFilter.class.getDeclaredMethod("doFilterInternal", HttpServletRequest.class, HttpServletResponse.class, FilterChain.class);
        method.setAccessible(true);
        method.invoke(instance, request, response, chain);
        Mockito.verify(chain).doFilter(request, response);

        Assertions.assertNotNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication failed");
    }

    @Test
    public void testNoTokenPresent() throws ServletException, IOException, ReflectiveOperationException {
        JwtTokenService service = getService();
        Mockito.when(service.getAccessToken(Mockito.any(HttpServletRequest.class))).thenReturn(Optional.empty());

        FilterChain chain = Mockito.mock(FilterChain.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        JwtAuthenticationTokenFilter instance = getNewInstance(service);

        Method method = JwtAuthenticationTokenFilter.class.getDeclaredMethod("doFilterInternal", HttpServletRequest.class, HttpServletResponse.class, FilterChain.class);
        method.setAccessible(true);
        method.invoke(instance, request, response, chain);
        Mockito.verify(chain).doFilter(request, response);

        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication succeeded");
    }

    @Test
    public void testParseFailed() throws ServletException, IOException, ReflectiveOperationException {
        JwtTokenService service = getService();
        Mockito.when(service.getAccessToken(Mockito.any(HttpServletRequest.class))).thenReturn(Optional.of("foobar"));
        Mockito.when(service.parseUser(Mockito.any())).thenReturn(Optional.empty());

        FilterChain chain = Mockito.mock(FilterChain.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        JwtAuthenticationTokenFilter instance = getNewInstance(service);

        Method method = JwtAuthenticationTokenFilter.class.getDeclaredMethod("doFilterInternal", HttpServletRequest.class, HttpServletResponse.class, FilterChain.class);
        method.setAccessible(true);
        method.invoke(instance, request, response, chain);
        Mockito.verify(chain).doFilter(request, response);

        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication succeeded");
    }
}
