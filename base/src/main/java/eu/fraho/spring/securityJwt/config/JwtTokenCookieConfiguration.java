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

@ConfigurationProperties(prefix = "fraho.jwt.token.cookie")
@Component
@Data
@Slf4j
public class JwtTokenCookieConfiguration implements CookieConfiguration {
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
    private String[] names = new String[]{"JWT-ACCESSTOKEN", "XSRF-TOKEN"};

    /**
     * @see javax.servlet.http.Cookie#setPath(String)
     */
    private String path = "/";

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

    public Logger getLog() {
        return log;
    }
}
