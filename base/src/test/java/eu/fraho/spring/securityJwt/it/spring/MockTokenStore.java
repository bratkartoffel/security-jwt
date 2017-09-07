/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.it.spring;

import eu.fraho.spring.securityJwt.dto.JwtUser;
import eu.fraho.spring.securityJwt.dto.RefreshToken;
import eu.fraho.spring.securityJwt.dto.TimeWithPeriod;
import eu.fraho.spring.securityJwt.service.RefreshTokenStore;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
@Slf4j
public class MockTokenStore implements RefreshTokenStore {
    private String activeToken = null;
    private JwtUser activeUser = null;

    @Override
    public void saveToken(@NotNull JwtUser user, @NotNull String token) {
        activeToken = token;
        activeUser = user;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends JwtUser> Optional<T> useToken(@NotNull String token) {
        Optional<T> result = Optional.empty();
        if (Objects.equals(token, activeToken)) {
            result = Optional.ofNullable((T) activeUser);
            activeToken = null;
            activeUser = null;
        }
        return result;
    }

    @Override
    public @NotNull List<RefreshToken> listTokens(@NotNull JwtUser user) {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public Map<Long, List<RefreshToken>> listTokens() {
        return Collections.emptyMap();
    }

    @Override
    public boolean revokeToken(@NotNull String token) {
        return false;
    }

    @Override
    public int revokeTokens(@NotNull JwtUser user) {
        return 0;
    }

    @Override
    public int revokeTokens() {
        return 0;
    }

    @NotNull
    @Deprecated
    public TimeWithPeriod getRefreshExpiration() {
        return new TimeWithPeriod(0, TimeUnit.SECONDS);
    }

    @Override
    public void afterPropertiesSet() {
        log.info("Using mock implementation to handle refresh tokens");
    }
}
