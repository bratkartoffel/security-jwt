/*
 * MIT Licence
 * Copyright (c) 2020 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.controller;

import eu.fraho.spring.securityJwt.base.config.RefreshCookieProperties;
import eu.fraho.spring.securityJwt.base.config.TokenCookieProperties;
import eu.fraho.spring.securityJwt.base.dto.AuthenticationResponse;
import eu.fraho.spring.securityJwt.base.dto.RefreshRequest;
import eu.fraho.spring.securityJwt.base.service.JwtTokenService;
import eu.fraho.spring.securityJwt.base.service.RefreshService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@RestController
@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@NoArgsConstructor
@ConditionalOnMissingBean(name = "refreshTokenStore", type = "eu.fraho.spring.securityJwt.base.service.NullTokenStore")
public class RefreshRestController implements CookieSupport {
    private JwtTokenService jwtTokenService;

    private TokenCookieProperties tokenCookieProperties;

    private RefreshCookieProperties refreshCookieProperties;

    private RefreshService refreshService;

    @RequestMapping("${fraho.jwt.refresh.path:/auth/refresh}")
    @Operation(summary = "Use a previously fetched refresh token to create a new access token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Generated token"),
            @ApiResponse(responseCode = "400", description = "Missing a required field in the request or refresh tokens not supported"),
            @ApiResponse(responseCode = "401", description = "Either the token expired, or the user has no longer access to this api"),
    })
    public ResponseEntity<AuthenticationResponse> refresh(HttpServletResponse response,
                                                          HttpServletRequest request,
                                                          @RequestBody(required = false) RefreshRequest refreshRequest) {
        log.debug("Starting refresh");

        // extract the refreshtoken from the body
        log.debug("Extracting token from request body");
        Optional<String> token = Optional.ofNullable(refreshRequest).map(RefreshRequest::getRefreshToken);
        if (!token.isPresent()) {
            log.debug("No refreshtoken in body found, trying to read it from the cookies");
            token = jwtTokenService.getRefreshToken(request);
        }

        AuthenticationResponse result = refreshService.checkRefresh(token.orElse(null));

        log.debug("Sending cookies if enabled");
        addTokenCookieIfEnabled(response, result.getAccessToken(), tokenCookieProperties);
        addTokenCookieIfEnabled(response, result.getRefreshToken(), refreshCookieProperties);

        // Return the token
        log.info("Successfully used refresh token");
        return ResponseEntity.ok(result);
    }

    @Autowired
    public void setJwtTokenService(@NonNull JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Autowired
    public void setTokenCookieProperties(@NonNull TokenCookieProperties tokenCookieProperties) {
        this.tokenCookieProperties = tokenCookieProperties;
    }

    @Autowired
    public void setRefreshCookieProperties(@NonNull RefreshCookieProperties refreshCookieProperties) {
        this.refreshCookieProperties = refreshCookieProperties;
    }

    @Autowired
    public void setRefreshService(@NonNull RefreshService refreshService) {
        this.refreshService = refreshService;
    }
}
