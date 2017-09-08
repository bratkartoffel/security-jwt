/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.security.base.controller;

import com.nimbusds.jose.JOSEException;
import eu.fraho.spring.security.base.dto.*;
import eu.fraho.spring.security.base.service.JwtTokenService;
import eu.fraho.spring.security.base.service.TotpService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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

import java.util.Optional;

@RestController
@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthenticationRestController {
    @NonNull
    private final AuthenticationManager authenticationManager;

    @NonNull
    private final JwtTokenService jwtTokenUtil;

    @NonNull
    private final UserDetailsService userDetailsService;

    @NonNull
    private final TotpService totpService;

    @RequestMapping("${fraho.jwt.refresh.path:/auth/refresh}")
    @ApiOperation("Use a previously fetched refresh token to create a new access token")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Generated token"),
            @ApiResponse(code = 400, message = "Missing a required field in the request or refresh tokens not supported"),
            @ApiResponse(code = 401, message = "Either the token expired, or the user has no longer access to this api"),
    })
    public ResponseEntity<JwtAuthenticationResponse> refresh(@RequestBody JwtRefreshRequest refreshRequest) throws JOSEException {
        if (!jwtTokenUtil.isRefreshTokenSupported()) {
            log.info("Refresh token support is disabled");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Optional<JwtUser> jwtUser = jwtTokenUtil.useRefreshToken(refreshRequest.getRefreshToken());
        if (!jwtUser.isPresent()) {
            log.info("Using refresh token failed");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        final JwtUser userDetails = jwtUser.get();
        log.debug("User {} successfully used refresh token, checking database", userDetails.getUsername());

        if (!userDetails.isApiAccessAllowed()) {
            log.info("User {} may no longer access api", userDetails.getUsername());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        log.debug("Generating new tokens for {}", userDetails.getUsername());
        final AccessToken accessToken = jwtTokenUtil.generateToken(userDetails);
        final RefreshToken refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);

        // Return the token
        JwtAuthenticationResponse body = new JwtAuthenticationResponse(accessToken, refreshToken);
        log.info("Successfully used refresh token for {}", userDetails.getUsername());
        return ResponseEntity.ok(body);
    }

    @RequestMapping("${fraho.jwt.token.path:/auth/login}")
    @ApiOperation("Create a new token using the supplied credentials")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Generated token"),
            @ApiResponse(code = 400, message = "Missing a required field in the request"),
            @ApiResponse(code = 401, message = "Either the credentials are wrong or the user has no access to this api"),
    })
    public ResponseEntity<JwtAuthenticationResponse> login(@RequestBody JwtAuthenticationRequest authenticationRequest) throws JOSEException {
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
            refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);
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
