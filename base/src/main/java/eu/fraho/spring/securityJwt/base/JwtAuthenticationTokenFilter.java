/*
 * MIT Licence
 * Copyright (c) 2025 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base;

import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import eu.fraho.spring.securityJwt.base.service.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    private JwtTokenService jwtTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        jwtTokenService.getAccessToken(request).ifPresent(t -> handleToken(t, request));
        chain.doFilter(request, response);
    }

    protected void handleToken(String token, HttpServletRequest request) {
        log.debug("AccessToken was present in request, extracting userdetails");
        jwtTokenService.parseUser(token).ifPresent(u -> handleUser(u, request));
    }

    protected void handleUser(JwtUser jwtUser, HttpServletRequest request) {
        log.debug("Successfully used token to authenticate {}", jwtUser.getUsername());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(jwtUser, "JWT", jwtUser.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Autowired
    public void setJwtTokenService(@NonNull JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }
}