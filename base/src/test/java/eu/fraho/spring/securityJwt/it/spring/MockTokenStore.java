/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.it.spring;

import eu.fraho.spring.securityJwt.dto.RefreshToken;
import eu.fraho.spring.securityJwt.dto.TimeWithPeriod;
import eu.fraho.spring.securityJwt.service.RefreshTokenStore;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
@Slf4j
public class MockTokenStore implements RefreshTokenStore {
    private String activeToken = null;

    @Override
    public void saveToken(@NotNull String username, @NotNull String deviceId, @NotNull String token) {
        activeToken = username + deviceId + token;
    }

    @Override
    public boolean useToken(@NotNull String username, @NotNull String deviceId, @NotNull String token) {
        String toCheck = username + deviceId + token;
        return Optional.ofNullable(activeToken).map(toCheck::equals).orElse(false);
    }

    @NotNull
    @Override
    public List<RefreshToken> listTokens(@NotNull String username) {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public Map<String, List<RefreshToken>> listTokens() {
        return Collections.emptyMap();
    }

    @Override
    public boolean revokeToken(@NotNull String username, @NotNull RefreshToken token) {
        return false;
    }

    @Override
    public int revokeTokens(@NotNull String username) {
        return 0;
    }

    @Override
    public boolean revokeToken(@NotNull String username, @NotNull String deviceId) {
        return false;
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
