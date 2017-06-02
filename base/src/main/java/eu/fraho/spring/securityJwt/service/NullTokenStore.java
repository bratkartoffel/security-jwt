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

import java.util.List;
import java.util.Map;

@Slf4j
class NullTokenStore implements RefreshTokenStore {
    @Override
    public void saveToken(String username, String deviceId, String token) {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @Override
    public boolean useToken(String username, String deviceId, String token) {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @Override
    public List<RefreshToken> listTokens(String username) {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @Override
    public Map<String, List<RefreshToken>> listTokens() {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @Override
    public boolean revokeToken(String username, RefreshToken token) {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @Override
    public int revokeTokens(String username) {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @Override
    public boolean revokeToken(String username, String deviceId) {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @Override
    public int revokeTokens() {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    public TimeWithPeriod getRefreshExpiration() {
        throw new FeatureNotConfiguredException("No implementation configured");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Using null implementation to handle refresh tokens");
    }
}
