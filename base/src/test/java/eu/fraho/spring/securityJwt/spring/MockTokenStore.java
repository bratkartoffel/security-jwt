package eu.fraho.spring.securityJwt.spring;

import eu.fraho.spring.securityJwt.dto.RefreshToken;
import eu.fraho.spring.securityJwt.dto.TimeWithPeriod;
import eu.fraho.spring.securityJwt.service.RefreshTokenStore;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MockTokenStore implements RefreshTokenStore {
    private Optional<String> activeToken = Optional.empty();

    @Override
    public void saveToken(String username, String deviceId, String token) {
        activeToken = Optional.of(username + deviceId + token);
    }

    @Override
    public boolean useToken(String username, String deviceId, String token) {
        String toCheck = username + deviceId + token;
        return activeToken.map(toCheck::equals).orElse(false);
    }

    @Override
    public List<RefreshToken> listTokens(String username) {
        return Collections.emptyList();
    }

    @Override
    public Map<String, List<RefreshToken>> listTokens() {
        return Collections.emptyMap();
    }

    @Override
    public boolean revokeToken(String username, RefreshToken token) {
        return false;
    }

    @Override
    public int revokeTokens(String username) {
        return 0;
    }

    @Override
    public boolean revokeToken(String username, String deviceId) {
        return false;
    }

    @Override
    public int revokeTokens() {
        return 0;
    }

    public TimeWithPeriod getRefreshExpiration() {
        return new TimeWithPeriod(0, TimeUnit.SECONDS);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Using mock implementation to handle refresh tokens");
    }
}
