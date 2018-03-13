/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.controller;

import com.nimbusds.jose.JOSEException;
import eu.fraho.spring.securityJwt.config.CookieProperties;
import eu.fraho.spring.securityJwt.config.RefreshProperties;
import eu.fraho.spring.securityJwt.config.TokenProperties;
import eu.fraho.spring.securityJwt.dto.*;
import eu.fraho.spring.securityJwt.service.JwtTokenService;
import eu.fraho.spring.securityJwt.service.TotpService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@RestController
@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRestController {
    @Setter(onMethod = @__({@Autowired, @NonNull}))
    private AuthenticationManager authenticationManager;

    @Setter(onMethod = @__({@Autowired, @NonNull}))
    private JwtTokenService jwtTokenService;

    @Setter(onMethod = @__({@Autowired, @NonNull}))
    private UserDetailsService userDetailsService;

    @Setter(onMethod = @__({@Autowired, @NonNull}))
    private TotpService totpService;

    @Setter(onMethod = @__({@Autowired, @NonNull}))
    private TokenProperties tokenProperties;

    @Setter(onMethod = @__({@Autowired, @NonNull}))
    private RefreshProperties refreshProperties;

    @SuppressWarnings("MVCPathVariableInspection")
    @RequestMapping("${fraho.jwt.refresh.path:/auth/refresh}")
    @ApiOperation("Use a previously fetched refresh token to create a new access token")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Generated token"),
            @ApiResponse(code = 400, message = "Missing a required field in the request or refresh tokens not supported"),
            @ApiResponse(code = 401, message = "Either the token expired, or the user has no longer access to this api"),
    })
    public ResponseEntity<AuthenticationResponse> refresh(HttpServletResponse response,
                                                          HttpServletRequest request,
                                                          @RequestBody(required = false) @Nullable RefreshRequest refreshRequest) {
        if (!jwtTokenService.isRefreshTokenSupported()) {
            log.info("Refresh token support is disabled");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // extract the refreshtoken from the body
        Optional<String> token = Optional.ofNullable(refreshRequest).map(RefreshRequest::getRefreshToken);
        if (!token.isPresent()) {
            log.debug("No refreshtoken in body found, trying to read it from the cookies");
            token = jwtTokenService.getRefreshToken(request);
        }

        // if no token was found at all, then abort with an unauthorized error
        if (!token.isPresent()) {
            log.info("No refresh token found in request");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // use the refresh token to get the underlying userdetails
        Optional<JwtUser> jwtUser = jwtTokenService.useRefreshToken(token.get());
        if (!jwtUser.isPresent()) {
            log.info("Using refresh token failed (unknown refreshtoken?)");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        final JwtUser userDetails = jwtUser.get();
        log.debug("User {} successfully used refresh token, checking database", userDetails.getUsername());

        if (!userDetails.isApiAccessAllowed()) {
            log.info("User {} may no longer access api", userDetails.getUsername());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        log.debug("Generating new tokens for {}", userDetails.getUsername());
        final AccessToken accessToken;
        try {
            accessToken = jwtTokenService.generateToken(userDetails);
        } catch (JOSEException e) {
            log.info("Error creating an access token for {}", userDetails.getUsername(), e);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        final RefreshToken refreshToken = jwtTokenService.generateRefreshToken(userDetails);

        log.debug("Sending cookies if enabled");
        addTokenCookieIfEnabled(response, accessToken, tokenProperties.getCookie());
        addTokenCookieIfEnabled(response, refreshToken, refreshProperties.getCookie());

        // Return the token
        log.info("Successfully used refresh token for {}", userDetails.getUsername());
        return ResponseEntity.ok(AuthenticationResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build());
    }

    @SuppressWarnings("MVCPathVariableInspection")
    @RequestMapping("${fraho.jwt.token.path:/auth/login}")
    @ApiOperation("Create a new token using the supplied credentials")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Generated token"),
            @ApiResponse(code = 400, message = "Missing a required field in the request"),
            @ApiResponse(code = 401, message = "Either the credentials are wrong or the user has no access to this api"),
    })
    public ResponseEntity<AuthenticationResponse> login(HttpServletResponse response,
                                                        @RequestBody AuthenticationRequest authenticationRequest) {
        // Perform the basic security
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getUsername(),
                        authenticationRequest.getPassword()
                )
        );
        log.info("Successfully authenticated against database for {}", authenticationRequest.getUsername());

        // Load the userdetails from the backend
        final JwtUser userDetails = (JwtUser) userDetailsService.loadUserByUsername(authenticationRequest.getUsername());

        // Verify that the user may access this api and his TOTP (if present / provided) is valid
        if (!userDetails.isApiAccessAllowed() || !isTotpOk(authenticationRequest, userDetails)) {
            log.info("User {} may not access api or the provided TOTP is invalid", userDetails.getUsername());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        log.debug("Setting SecurityContext");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("Generating tokens");
        final AccessToken accessToken;
        try {
            accessToken = jwtTokenService.generateToken(userDetails);
        } catch (JOSEException e) {
            log.info("Error creating an access token for {}", userDetails.getUsername(), e);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        final RefreshToken refreshToken;
        if (jwtTokenService.isRefreshTokenSupported()) {
            log.debug("Generating refreshtoken");
            refreshToken = jwtTokenService.generateRefreshToken(userDetails);
        } else {
            refreshToken = null;
        }

        // Send the cookies if enabled by configuration
        addTokenCookieIfEnabled(response, accessToken, tokenProperties.getCookie());
        addTokenCookieIfEnabled(response, refreshToken, refreshProperties.getCookie());

        // Return the token
        log.info("Successfully created token for {}", userDetails.getUsername());
        return ResponseEntity.ok(AuthenticationResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build());
    }

    private boolean isTotpOk(AuthenticationRequest authenticationRequest, JwtUser userDetails) {
        return userDetails.getTotpSecret().map(secret ->
                authenticationRequest.getTotp().map(code ->
                        totpService.verifyCode(secret, code)
                ).orElse(false) // user has totp, but none in request = nok
        ).orElse(true); // user has no secret = ok
    }

    private void addTokenCookieIfEnabled(HttpServletResponse response, @Nullable AbstractToken token, CookieProperties configuration) {
        if (configuration.isEnabled() && token != null) {
            Cookie cookie = new Cookie(configuration.getNames()[0], token.getToken());
            Optional.ofNullable(configuration.getDomain()).ifPresent(cookie::setDomain);
            Optional.ofNullable(configuration.getPath()).ifPresent(cookie::setPath);
            cookie.setSecure(configuration.isSecure());
            cookie.setHttpOnly(configuration.isHttpOnly());
            cookie.setMaxAge(token.getExpiresIn());
            response.addCookie(cookie);
            log.debug("Sending cookie: name={}, secure={}, path={}, httponly={}", cookie.getName(), cookie.getSecure(), cookie.getPath(), cookie.isHttpOnly());
        }
    }
}
