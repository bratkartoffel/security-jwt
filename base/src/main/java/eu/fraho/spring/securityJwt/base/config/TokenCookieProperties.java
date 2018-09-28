/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
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

@ConfigurationProperties(prefix = "fraho.jwt.token.cookie")
@Component
@Getter
@Setter
@Slf4j
public class TokenCookieProperties implements CookieProperties {
    /**
     * Enables support for tokens sent as a cookie.
     * <p>
     * Please not that tokens sent as headers always take precedence.
     */
    private boolean enabled = false;

    /**
     * Sets the name of the cookie with the token.
     * <p>
     * The first entry in this list is used when sending out the cookie, any other
     * names are optionally taken when validating incoming requests.
     */

    @NonNull
    private String[] names = new String[]{"JWT-ACCESSTOKEN", "XSRF-TOKEN"};

    /**
     * The issued access token cookie will only be sent by the client to URIs matching this pattern.
     *
     * @see javax.servlet.http.Cookie#setPath(String)
     */

    @NonNull
    private String path = "/";

    /**
     * The issued tokens will only be valid for the specified domain. Defaults to the issuing server domain.
     *
     * @see javax.servlet.http.Cookie#setDomain(String)
     */

    private String domain;

    /**
     * The cookie will not be accessible by client JavaScript if enabled (highly recommend)
     *
     * @see javax.servlet.http.Cookie#setHttpOnly(boolean)
     */
    private boolean httpOnly = true;

    /**
     * The cookie will only be sent over an encrypted (https) connection (highly recommend)
     *
     * @see javax.servlet.http.Cookie#setSecure(boolean)
     */
    private boolean secure = true;

    public Logger getLog() {
        return log;
    }
}
