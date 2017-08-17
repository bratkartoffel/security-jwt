/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.service;

import eu.fraho.spring.securityJwt.dto.RefreshToken;
import eu.fraho.spring.securityJwt.dto.TimeWithPeriod;
import eu.fraho.spring.securityJwt.exceptions.FeatureNotConfiguredException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

@Slf4j
public class NullTokenStore implements RefreshTokenStore {
    @Override
    public void saveToken(@NotNull String username, @NotNull String deviceId, @NotNull String token) {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @Override
    public boolean useToken(@NotNull String username, @NotNull String deviceId, @NotNull String token) {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @NotNull
    @Override
    public List<RefreshToken> listTokens(@NotNull String username) {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @NotNull
    @Override
    public Map<String, List<RefreshToken>> listTokens() {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @Override
    public boolean revokeToken(@NotNull String username, @NotNull RefreshToken token) {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @Override
    public int revokeTokens(@NotNull String username) {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @Override
    public boolean revokeToken(@NotNull String username, @NotNull String deviceId) {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @Override
    public int revokeTokens() {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @NotNull
    public TimeWithPeriod getRefreshExpiration() {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Using null implementation to handle refresh tokens");
    }
}
