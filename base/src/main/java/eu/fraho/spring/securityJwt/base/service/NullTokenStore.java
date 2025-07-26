/*
 * MIT Licence
 * Copyright (c) 2025 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.service;

import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import eu.fraho.spring.securityJwt.base.dto.RefreshToken;
import eu.fraho.spring.securityJwt.base.exceptions.FeatureNotConfiguredException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class NullTokenStore implements RefreshTokenStore {
    @Override
    public void saveToken(JwtUser user, String token) {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @Override
    public <T extends JwtUser> Optional<T> useToken(String token) {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @Override
    public List<RefreshToken> listTokens(JwtUser user) {
        throw new FeatureNotConfiguredException("No implementation configured");
    }


    @Override
    public Map<Long, List<RefreshToken>> listTokens() {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @Override
    public boolean revokeToken(String token) {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @Override
    public int revokeTokens(JwtUser user) {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @Override
    public int revokeTokens() {
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
