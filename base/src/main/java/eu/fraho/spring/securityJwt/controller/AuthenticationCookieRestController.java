/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.controller;

import com.nimbusds.jose.JOSEException;
import eu.fraho.spring.securityJwt.config.JwtRefreshCookieConfiguration;
import eu.fraho.spring.securityJwt.dto.JwtAuthenticationResponse;
import eu.fraho.spring.securityJwt.dto.JwtRefreshRequest;
import eu.fraho.spring.securityJwt.service.JwtTokenService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthenticationCookieRestController {
    @NonNull
    private final JwtTokenService jwtTokenUtil;

    @NonNull
    private final JwtRefreshCookieConfiguration refreshCookieConfiguration;

    @NonNull
    private final AuthenticationRestController authenticationRestController;

    @RequestMapping("${fraho.jwt.refresh.cookie.path:/auth/refreshCookie}")
    @ApiOperation("Use a previously fetched refresh token to create a new access token. Used for regular applciations, where the refresh process should be automatically done by the browser.")
    @ApiResponses({
            @ApiResponse(code = 303, message = "After successfully using the refresh token the client will be redirected to his previous page"),
            @ApiResponse(code = 400, message = "Missing the refresh token cookie"),
            @ApiResponse(code = 401, message = "Either the token expired, or the user has no longer access to this api"),
    })
    public ResponseEntity<Object> refresh(HttpServletRequest request,
                                          HttpServletResponse response) throws JOSEException {
        if (!jwtTokenUtil.isRefreshTokenSupported() || !refreshCookieConfiguration.isEnabled()) {
            log.warn("Refresh cookie support is disabled, but user tried anyways");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return jwtTokenUtil.getRefreshToken(request).map(token -> {
            ResponseEntity<JwtAuthenticationResponse> result =
                    authenticationRestController.refresh(response, new JwtRefreshRequest(token));

            if (result.getStatusCode() == HttpStatus.OK) {
                Optional<String> referer = getReferer(request);
                HttpStatus status = referer.map(s -> HttpStatus.TEMPORARY_REDIRECT).orElse(HttpStatus.SEE_OTHER);
                String location = referer.orElse(refreshCookieConfiguration.getFallbackRedirectUrl());
                log.info("Redirecting user to {}", location);
                return ResponseEntity
                        .status(status)
                        .header("Location", location)
                        .build();
            }

            return ResponseEntity.status(result.getStatusCode()).build();
        }).orElse(new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    private Optional<String> getReferer(@NotNull HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Referer"));
    }
}
