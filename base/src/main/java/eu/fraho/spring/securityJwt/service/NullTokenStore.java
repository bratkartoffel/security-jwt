/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.service;

import eu.fraho.spring.securityJwt.dto.JwtUser;
import eu.fraho.spring.securityJwt.dto.RefreshToken;
import eu.fraho.spring.securityJwt.dto.TimeWithPeriod;
import eu.fraho.spring.securityJwt.exceptions.FeatureNotConfiguredException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class NullTokenStore implements RefreshTokenStore {
    @Override
    public void saveToken(@NotNull JwtUser user, @NotNull String token) {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @Override
    public <T extends JwtUser> Optional<T> useToken(@NotNull String token) {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @Override
    public @NotNull List<RefreshToken> listTokens(@NotNull JwtUser user) {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @NotNull
    @Override
    public Map<Long, List<RefreshToken>> listTokens() {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @Override
    public boolean revokeToken(@NotNull String token) {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @Override
    public int revokeTokens(@NotNull JwtUser user) {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @Override
    public int revokeTokens() {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @NotNull
    @Deprecated
    public TimeWithPeriod getRefreshExpiration() {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @Override
    public void afterPropertiesSet() {
        log.info("Using null implementation to handle refresh tokens");
    }

    @Override
    public boolean isRefreshTokenSupported() {
        return false;
    }
}
