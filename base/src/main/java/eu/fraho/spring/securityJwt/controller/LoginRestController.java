/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.controller;

import eu.fraho.spring.securityJwt.config.RefreshProperties;
import eu.fraho.spring.securityJwt.config.TokenProperties;
import eu.fraho.spring.securityJwt.dto.AuthenticationRequest;
import eu.fraho.spring.securityJwt.dto.AuthenticationResponse;
import eu.fraho.spring.securityJwt.service.LoginService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
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
@AllArgsConstructor
public class LoginRestController implements CookieSupport {
    @Setter(onMethod = @__({@Autowired, @NonNull}))
    private LoginService loginService;

    @Setter(onMethod = @__({@Autowired, @NonNull}))
    private TokenProperties tokenProperties;

    @Setter(onMethod = @__({@Autowired, @NonNull}))
    private RefreshProperties refreshProperties;

    @RequestMapping("${fraho.jwt.token.path:/auth/login}")
    @ApiOperation("Create a new token using the supplied credentials")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Generated token"),
            @ApiResponse(code = 400, message = "Missing a required field in the request"),
            @ApiResponse(code = 401, message = "Either the credentials are wrong or the user has no access to this api"),
    })
    public ResponseEntity<AuthenticationResponse> login(HttpServletResponse response,
                                                        @RequestBody AuthenticationRequest authenticationRequest) {
        log.debug("Starting login");
        AuthenticationResponse tokens = loginService.checkLogin(authenticationRequest);

        // Send the cookies if enabled by configuration
        log.debug("Sending cookies if enabled");
        addTokenCookieIfEnabled(response, tokens.getAccessToken(), tokenProperties.getCookie());
        addTokenCookieIfEnabled(response, tokens.getRefreshToken(), refreshProperties.getCookie());

        // Return the token
        log.info("Login successfully completed");
        return ResponseEntity.ok(tokens);
    }
}
