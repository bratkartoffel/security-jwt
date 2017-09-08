/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.security.base.it.spring;

import eu.fraho.spring.security.base.dto.JwtUser;
import eu.fraho.spring.security.base.dto.RefreshToken;
import eu.fraho.spring.security.base.dto.TimeWithPeriod;
import eu.fraho.spring.security.base.service.RefreshTokenStore;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.*;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MockTokenStore implements RefreshTokenStore {
    private String activeToken = null;
    private JwtUser activeUser = null;

    @NonNull
    private UserDetailsService userDetailsService;

    @Override
    public void saveToken(@NotNull JwtUser user, @NotNull String token) {
        activeToken = token;
        activeUser = user;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<JwtUser> useToken(@NotNull String token) {
        Optional<JwtUser> result = Optional.empty();
        if (Objects.equals(token, activeToken)) {
            result = Optional.ofNullable(activeUser)
                    .map(JwtUser::getUsername)
                    .map(userDetailsService::loadUserByUsername)
                    .map(JwtUser.class::cast);
            activeToken = null;
            activeUser = null;
        }
        return result;
    }

    @Override
    @NotNull
    public List<RefreshToken> listTokens(@NotNull JwtUser user) {
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
