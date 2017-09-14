/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "fraho.jwt.refresh.cookie")
@Component
@Data
@Slf4j
public class JwtRefreshCookieConfiguration implements CookieConfiguration {
    /**
     * Enables support for tokens sent as a cookie.
     * <p>
     * Please note that tokens sent as headers always take precedence.
     */
    private boolean enabled = false;

    /**
     * Sets the name of the cookie with the token.
     * <p>
     * The first entry in this list is used when sending out the cookie, any other
     * names are optionally taken when validating incoming requests.
     */
    private String[] names = new String[]{"JWT-REFRESHTOKEN"};

    /**
     * @see javax.servlet.http.Cookie#setDomain(String)
     */
    private String domain;

    /**
     * @see javax.servlet.http.Cookie#setHttpOnly(boolean)
     */
    private boolean httpOnly = true;

    /**
     * @see javax.servlet.http.Cookie#setSecure(boolean)
     */
    private boolean secure = true;

    /**
     * After using the refresh token redirect user to his referer
     * or (if unavailable for any reason) to this path.
     * <p>
     * The path may be a full URI.
     */
    private String fallbackRedirectUrl = "/";

    /**
     * Sets the path for the RestController, defining the endpoint for refresh requests.
     * This endpoint has to be different to @{link {@link JwtRefreshConfiguration#path}}
     */
    private String path = "/auth/refreshCookie";

    public Logger getLog() {
        return log;
    }
}
