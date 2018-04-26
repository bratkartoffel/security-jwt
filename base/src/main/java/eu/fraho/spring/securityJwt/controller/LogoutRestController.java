/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.controller;

import eu.fraho.spring.securityJwt.config.RefreshProperties;
import eu.fraho.spring.securityJwt.config.TokenProperties;
import eu.fraho.spring.securityJwt.dto.AccessToken;
import eu.fraho.spring.securityJwt.dto.RefreshToken;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@ConditionalOnExpression("'${fraho.jwt.token.cookie.enabled}' == 'true' or '${refreshCookieProperties.enabled}' == 'true'")
public class LogoutRestController implements CookieSupport {
    @Setter(onMethod = @__({@Autowired, @NonNull}))
    private TokenProperties tokenProperties;

    @Setter(onMethod = @__({@Autowired, @NonNull}))
    private RefreshProperties refreshProperties;

    @RequestMapping("${fraho.jwt.logout.path:/auth/logout}")
    @ApiOperation("Deleted the sent out cookies, thus resulting in an logout")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Logout successfull"),
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletResponse response) {
        log.debug("Starting logout");

        // Send the cookies if enabled by configuration
        log.debug("Sending cookies if enabled");
        addTokenCookieIfEnabled(response, AccessToken.builder().token("dummy").expiresIn(0).build(), tokenProperties.getCookie());
        addTokenCookieIfEnabled(response, RefreshToken.builder().token("dummy").expiresIn(0).build(), refreshProperties.getCookie());
        log.debug("Logout finished");
    }
}
