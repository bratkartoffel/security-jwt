/*
 * MIT Licence
 * Copyright (c) 2025 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.controller;

import eu.fraho.spring.securityJwt.base.config.RefreshCookieProperties;
import eu.fraho.spring.securityJwt.base.config.TokenCookieProperties;
import eu.fraho.spring.securityJwt.base.dto.AccessToken;
import eu.fraho.spring.securityJwt.base.dto.RefreshToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@NoArgsConstructor
@ConditionalOnExpression("'${fraho.jwt.token.cookie.enabled}' == 'true' or '${fraho.jwt.refresh.cookie.enabled}' == 'true'")
@Tag(name = "Authentication")
public class LogoutRestController implements CookieSupport {
    private TokenCookieProperties tokenCookieProperties;

    private RefreshCookieProperties refreshCookieProperties;

    @RequestMapping("${fraho.jwt.logout.path:/auth/logout}")
    @Operation(summary = "Delete the cookies, thus resulting in an logout")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Logout successfull"),
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletResponse response) {
        log.debug("Starting logout");

        // Send the cookies if enabled by configuration
        log.debug("Sending cookies if enabled");
        addTokenCookieIfEnabled(response, AccessToken.builder().token("dummy").expiresIn(0).build(), tokenCookieProperties);
        addTokenCookieIfEnabled(response, RefreshToken.builder().token("dummy").expiresIn(0).build(), refreshCookieProperties);
        log.debug("Logout finished");
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
