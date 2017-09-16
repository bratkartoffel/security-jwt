/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt;

import eu.fraho.spring.securityJwt.config.JwtRefreshCookieConfiguration;
import eu.fraho.spring.securityJwt.service.JwtTokenService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @NonNull
    private final JwtRefreshCookieConfiguration refreshCookieConfiguration;

    @NonNull
    private final JwtTokenService jwtTokenService;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        if (refreshCookieConfiguration.isEnabled() && jwtTokenService.getRefreshToken(request).isPresent()) {
            String url;
            if (refreshCookieConfiguration.getDomain() != null) {
                try {
                    URI uri = new URI(
                            refreshCookieConfiguration.isSecure() ? "https" : "http",
                            null,
                            refreshCookieConfiguration.getDomain(),
                            refreshCookieConfiguration.getPort(),
                            refreshCookieConfiguration.getPath(),
                            null, null);
                    url = uri.toString();
                } catch (URISyntaxException use) {
                    log.error("Error creating redirect URI, using only path", use);
                    url = refreshCookieConfiguration.getPath();
                }
            } else {
                url = refreshCookieConfiguration.getPath();
            }

            log.debug("Cookie refresh finished, redirecting to {}", url);
            response.sendRedirect(url);
        } else {
            // We should just send a 401 Unauthorized response because there is no 'login page' to redirect to
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
    }
}