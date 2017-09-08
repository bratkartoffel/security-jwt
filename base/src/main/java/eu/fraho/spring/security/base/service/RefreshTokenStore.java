/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.security.base.service;

import eu.fraho.spring.security.base.dto.JwtUser;
import eu.fraho.spring.security.base.dto.RefreshToken;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RefreshTokenStore extends InitializingBean {
    void saveToken(@NotNull JwtUser user, @NotNull String token);

    <T extends JwtUser> Optional<T> useToken(@NotNull String token);

    @NotNull
    List<RefreshToken> listTokens(@NotNull JwtUser user);

    @NotNull
    Map<Long, List<RefreshToken>> listTokens();

    boolean revokeToken(@NotNull String token);

    int revokeTokens(@NotNull JwtUser user);

    int revokeTokens();

    /**
     * Ask this service if refresh token support is enabled
     * by a third-party addon.
     *
     * @return {@code true} when refresh tokens are supported
     */
    default boolean isRefreshTokenSupported() {
        return true;
    }
}
