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
     * If this value is not specified, then the redirect for refreshing the cookies will be sent only with
     * the {@link #path} specified. Otherweise the domain will be prepended to the redirect, thus allowing
     * to use an external refresh server.
     *
     * @see javax.servlet.http.Cookie#setDomain(String)
     */
    private String domain;

    /**
     * When the client requests a resource with an expired token, he will be redirected to the authentication
     * server to automatically obtain new token cookies.
     * <p>
     * If the authentication server don't run on default http(s) port, then you can use this property to
     * set the portnumber. A value of -1 means that no portnumber will be used in the redirect URI.
     */
    private int port = -1;

    /**
     * The cookie will not be accessible by client JavaScript if enabled (highly recommend)
     *
     * @see javax.servlet.http.Cookie#setHttpOnly(boolean)
     */
    private boolean httpOnly = true;

    /**
     * The cookie will only be sent over an encrypted (https) connection (recommend)
     *
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
     * This path has to start with a /.
     * This endpoint has to be different to @{link {@link JwtRefreshConfiguration#path}}
     */
    private String path = "/auth/refreshCookie";

    public Logger getLog() {
        return log;
    }
}
