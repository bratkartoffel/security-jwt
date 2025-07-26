/*
 * MIT Licence
 * Copyright (c) 2025 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.service;

import com.nimbusds.jose.JOSEException;
import eu.fraho.spring.securityJwt.base.dto.AccessToken;
import eu.fraho.spring.securityJwt.base.dto.AuthenticationRequest;
import eu.fraho.spring.securityJwt.base.dto.AuthenticationResponse;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import eu.fraho.spring.securityJwt.base.dto.RefreshToken;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
@NoArgsConstructor
public class LoginServiceImpl implements LoginService {
    private AuthenticationManager authenticationManager;

    private JwtTokenService jwtTokenService;

    private UserDetailsService userDetailsService;

    private TotpService totpService;

    @Override
    public AuthenticationResponse checkLogin(AuthenticationRequest authenticationRequest) throws AuthenticationException {
        // Perform the basic security
        Authentication authentication = tryAuthentication(authenticationRequest);
        log.info("Successfully authenticated against database for {}", authenticationRequest.getUsername());

        // Load the userdetails from the backend
        log.info("Fetching userdetails from backend");
        JwtUser userDetails = (JwtUser) userDetailsService.loadUserByUsername(authenticationRequest.getUsername());

        // Verify that the user may access this api and his TOTP (if present / provided) is valid
        log.info("Checking api access right and totp");
        if (!userDetails.isApiAccessAllowed() || !isTotpOk(authenticationRequest.getTotp().orElse(null), userDetails)) {
            log.info("User {} may not access api or the provided TOTP is invalid", userDetails.getUsername());
            throw new BadCredentialsException("Invalid TOTP or insufficient access rights");
        }

        log.debug("Everything ok, setting SecurityContext");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("Generating access token");
        AccessToken accessToken;
        try {
            accessToken = jwtTokenService.generateToken(userDetails);
        } catch (JOSEException e) {
            log.info("Error creating an access token for {}", userDetails.getUsername(), e);
            throw new BadCredentialsException("Token generation failed");
        }

        RefreshToken refreshToken;
        if (jwtTokenService.isRefreshTokenSupported()) {
            log.debug("Generating refresh token");
            refreshToken = jwtTokenService.generateRefreshToken(userDetails);
        } else {
            log.debug("Refresh tokens are disabled");
            refreshToken = null;
        }

        return AuthenticationResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

    protected Authentication tryAuthentication(AuthenticationRequest authenticationRequest) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getUsername(),
                        authenticationRequest.getPassword()
                )
        );
    }

    protected boolean isTotpOk(Integer totp, JwtUser userDetails) {
        return userDetails.getTotpSecret().map(secret -> {
                    log.debug("User has a totp secret set, let's check the supplied pin");
                    return Optional.ofNullable(totp).map(code -> {
                                boolean result = totpService.verifyCode(secret, code);
                                log.debug("Pin verification returned {}", result);
                                return result;
                            }
                    ).orElse(false); // user has totp, but none in request = nok
                }
        ).orElse(true); // user has no secret = ok
    }

    @Autowired
    public void setAuthenticationManager(@NonNull AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Autowired
    public void setJwtTokenService(@NonNull JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Autowired
    public void setUserDetailsService(@NonNull UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    public void setTotpService(@NonNull TotpService totpService) {
        this.totpService = totpService;
    }
}
