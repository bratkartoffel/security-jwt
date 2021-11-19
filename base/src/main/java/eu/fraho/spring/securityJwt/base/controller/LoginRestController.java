/*
 * MIT Licence
 * Copyright (c) 2021 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.controller;

import eu.fraho.spring.securityJwt.base.config.RefreshCookieProperties;
import eu.fraho.spring.securityJwt.base.config.TokenCookieProperties;
import eu.fraho.spring.securityJwt.base.dto.AuthenticationRequest;
import eu.fraho.spring.securityJwt.base.dto.AuthenticationResponse;
import eu.fraho.spring.securityJwt.base.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@NoArgsConstructor
@Tag(name = "Authentication")
public class LoginRestController implements CookieSupport {
    private LoginService loginService;

    private TokenCookieProperties tokenCookieProperties;

    private RefreshCookieProperties refreshCookieProperties;

    @RequestMapping("${fraho.jwt.token.path:/auth/login}")
    @Operation(summary = "Create a new token using the supplied credentials")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Generated token"),
            @ApiResponse(responseCode = "400", description = "Missing a required field in the request"),
            @ApiResponse(responseCode = "401", description = "Either the credentials are wrong or the user has no access to this api"),
    })
    public ResponseEntity<AuthenticationResponse> login(HttpServletResponse response,
                                                        @RequestBody AuthenticationRequest authenticationRequest) {
        log.debug("Starting login");
        AuthenticationResponse tokens = loginService.checkLogin(authenticationRequest);

        // Send the cookies if enabled by configuration
        log.debug("Sending cookies if enabled");
        addTokenCookieIfEnabled(response, tokens.getAccessToken(), tokenCookieProperties);
        addTokenCookieIfEnabled(response, tokens.getRefreshToken(), refreshCookieProperties);

        // Return the token
        log.info("Login successfully completed");
        return ResponseEntity.ok(tokens);
    }

    @Autowired
    public void setLoginService(@NonNull LoginService loginService) {
        this.loginService = loginService;
    }

    @Autowired
    public void setTokenCookieProperties(@NonNull TokenCookieProperties tokenCookieProperties) {
        this.tokenCookieProperties = tokenCookieProperties;
    }

    @Autowired
    public void setRefreshCookieProperties(@NonNull RefreshCookieProperties refreshCookieProperties) {
        this.refreshCookieProperties = refreshCookieProperties;
    }
}
