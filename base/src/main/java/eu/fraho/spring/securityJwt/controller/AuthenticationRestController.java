/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.controller;

import com.nimbusds.jose.JOSEException;
import eu.fraho.spring.securityJwt.dto.*;
import eu.fraho.spring.securityJwt.service.JwtTokenService;
import eu.fraho.spring.securityJwt.service.TotpService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
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

import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping(value = "/auth", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class AuthenticationRestController {
    @Autowired
    private AuthenticationManager authenticationManager = null;

    @Autowired
    private JwtTokenService jwtTokenUtil = null;

    @Autowired
    private UserDetailsService userDetailsService = null;

    @Autowired
    private TotpService totpService = null;

    @RequestMapping("/refresh")
    @ApiOperation("Use a previously fetched refresh token to create a new access token")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Generated token"),
            @ApiResponse(code = 400, message = "Missing a required field in the request"),
            @ApiResponse(code = 401, message = "Either the token expired, or the user has no longer access to this api"),
    })
    public ResponseEntity<JwtAuthenticationResponse> refresh(@RequestBody JwtRefreshRequest refreshRequest)
            throws JOSEException, TimeoutException {
        if (!jwtTokenUtil.useRefreshToken(refreshRequest.getUsername(), refreshRequest.getDeviceId().orElse(null), refreshRequest.getRefreshToken())) {
            log.info("Using refresh token failed for {}", refreshRequest.getUsername());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        final JwtUser userDetails = (JwtUser) userDetailsService.loadUserByUsername(refreshRequest.getUsername());
        if (!userDetails.isApiAccessAllowed()) {
            log.info("User {} may no longer access api", refreshRequest.getUsername());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        log.debug("Generating tokens");
        final AccessToken accessToken = jwtTokenUtil.generateToken(userDetails);
        final RefreshToken refreshToken;
        if (jwtTokenUtil.isRefreshTokenSupported()) {
            refreshToken = jwtTokenUtil.generateRefreshToken(refreshRequest.getUsername(), refreshRequest.getDeviceId().orElse(null));
        } else {
            refreshToken = null;
        }

        // Return the token
        JwtAuthenticationResponse body = new JwtAuthenticationResponse(accessToken, refreshToken);
        log.info("Successfully used refresh token for {}", userDetails.getUsername());
        return ResponseEntity.ok(body);
    }

    @RequestMapping("/login")
    @ApiOperation("Create a new token using the supplied credentials")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Generated token"),
            @ApiResponse(code = 400, message = "Missing a required field in the request"),
            @ApiResponse(code = 401, message = "Either the credentials are wrong or the user has no access to this api"),
    })
    public ResponseEntity<JwtAuthenticationResponse> login(@RequestBody JwtAuthenticationRequest authenticationRequest)
            throws JOSEException, TimeoutException {
        // Perform the security
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getUsername(),
                        authenticationRequest.getPassword()
                )
        );
        log.info("Successfully authenticated against database for {}", authenticationRequest.getUsername());

        // Reload password post-security so we can generate token
        final JwtUser userDetails = (JwtUser) userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        if (!userDetails.isApiAccessAllowed() || !isTotpOk(authenticationRequest, userDetails)) {
            log.info("User {} may not access api or the provided TOTP is invalid", userDetails.getUsername());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        log.debug("Setting SecurityContext");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("Generating tokens");
        final AccessToken accessToken = jwtTokenUtil.generateToken(userDetails);
        final RefreshToken refreshToken;
        if (jwtTokenUtil.isRefreshTokenSupported()) {
            refreshToken = jwtTokenUtil.generateRefreshToken(authenticationRequest.getUsername(), authenticationRequest.getDeviceId().orElse(null));
        } else {
            refreshToken = null;
        }
        // Return the token
        JwtAuthenticationResponse body = new JwtAuthenticationResponse(accessToken, refreshToken);
        log.info("Successfully created token for {}", userDetails.getUsername());
        return ResponseEntity.ok(body);
    }

    private boolean isTotpOk(JwtAuthenticationRequest authenticationRequest, JwtUser userDetails) {
        return userDetails.getTotpSecret().map(secret ->
                authenticationRequest.getTotp().map(code ->
                        totpService.verifyCode(secret, code)
                ).orElse(false)
        ).orElse(true);
    }
}
