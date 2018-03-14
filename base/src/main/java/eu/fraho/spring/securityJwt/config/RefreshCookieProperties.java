/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.config;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "fraho.jwt.refresh.cookie")
@Component
@Data
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
    @NotNull
    @NonNull
    private String[] names = new String[]{"JWT-REFRESHTOKEN"};

    /**
     * The cookie will only be sent to these domains.
     *
     * @see javax.servlet.http.Cookie#setDomain(String)
     */
    @Nullable
    private String domain;

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
     * The issued access token cookie will only be sent by the client to URIs matching this pattern.
     * <p>
     * This path spec has to include the endpoint for refreshing tokens, otherwise this won't work!
     *
     * @see javax.servlet.http.Cookie#setPath(String)
     */
    @NotNull
    @NonNull
    private String path = "/auth/refresh";

    public Logger getLog() {
        return log;
    }
}
