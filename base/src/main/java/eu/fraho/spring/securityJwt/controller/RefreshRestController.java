/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.controller;

import eu.fraho.spring.securityJwt.config.RefreshCookieProperties;
import eu.fraho.spring.securityJwt.config.TokenCookieProperties;
import eu.fraho.spring.securityJwt.dto.AuthenticationResponse;
import eu.fraho.spring.securityJwt.dto.RefreshRequest;
import eu.fraho.spring.securityJwt.service.JwtTokenService;
import eu.fraho.spring.securityJwt.service.RefreshService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
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
@ConditionalOnMissingBean(name = "refreshTokenStore", type = "eu.fraho.spring.securityJwt.service.NullTokenStore")
public class RefreshRestController implements CookieSupport {
    @Setter(onMethod = @__({@Autowired, @NonNull}))
    private JwtTokenService jwtTokenService;

    @Setter(onMethod = @__({@Autowired, @NonNull}))
    private TokenCookieProperties tokenCookieProperties;

    @Setter(onMethod = @__({@Autowired, @NonNull}))
    private RefreshCookieProperties refreshCookieProperties;

    @Setter(onMethod = @__({@Autowired, @NonNull}))
    private RefreshService refreshService;

    @RequestMapping("${fraho.jwt.refresh.path:/auth/refresh}")
    @ApiOperation("Use a previously fetched refresh token to create a new access token")
    @ApiResponses({
            @ApiResponse(code = HttpServletResponse.SC_OK, message = "Generated token"),
            @ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = "Missing a required field in the request or refresh tokens not supported"),
            @ApiResponse(code = HttpServletResponse.SC_UNAUTHORIZED, message = "Either the token expired, or the user has no longer access to this api"),
    })
    public ResponseEntity<AuthenticationResponse> refresh(HttpServletResponse response,
                                                          HttpServletRequest request,
                                                          @RequestBody(required = false) @Nullable RefreshRequest refreshRequest) {
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
}
