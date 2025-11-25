/*
 * MIT Licence
 * Copyright (c) 2025 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.config;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "fraho.jwt.refresh.cookie")
@Component
@Getter
@Setter
@Slf4j
public class RefreshCookieProperties implements CookieProperties {
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

    @NonNull
    private String[] names = new String[]{"JWT-REFRESHTOKEN"};

    /**
     * The cookie will only be sent to these domains.
     *
     * @see jakarta.servlet.http.Cookie#setDomain(String)
     */

    private String domain;

    /**
     * The cookie will not be accessible by client JavaScript if enabled (highly recommend)
     *
     * @see jakarta.servlet.http.Cookie#setHttpOnly(boolean)
     */
    private boolean httpOnly = true;

    /**
     * The cookie will only be sent over an encrypted (https) connection (recommend)
     *
     * @see jakarta.servlet.http.Cookie#setSecure(boolean)
     */
    private boolean secure = true;

    /**
     * The issued access token cookie will only be sent by the client to URIs matching this pattern.
     * <p>
     * This path spec has to include the endpoint for refreshing tokens, otherwise this won't work!
     *
     * @see jakarta.servlet.http.Cookie#setPath(String)
     */

    @NonNull
    private String path = "/auth/refresh";

    public Logger getLog() {
        return log;
    }
}
