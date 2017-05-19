package eu.fraho.spring.securityJwt.service;

import eu.fraho.spring.securityJwt.dto.RefreshToken;
import eu.fraho.spring.securityJwt.dto.TimeWithPeriod;
import eu.fraho.spring.securityJwt.exceptions.JwtRefreshException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Getter
public class InternalTokenStore implements RefreshTokenStore {
    @Value("${fraho.jwt.refresh.expiration:" + JwtTokenService.DEFAULT_REFRESH_EXPIRATION + "}")
    private TimeWithPeriod refreshExpiration = new TimeWithPeriod(JwtTokenService.DEFAULT_REFRESH_EXPIRATION);
    @Value("${fraho.jwt.refresh.cache.prefix:" + JwtTokenService.DEFAULT_CACHE_PREFIX + "}")
    private String refreshCachePrefix = JwtTokenService.DEFAULT_CACHE_PREFIX;

    private ExpiringMap<String, String> refreshTokenMap = null;

    @Override
    public void saveToken(String username, String deviceId, String token) {
        String key = String.format("%s:%s:%s", refreshCachePrefix, username, deviceId);
        refreshTokenMap.put(key, token);
    }

    @Override
    public boolean useToken(String username, String deviceId, String token) {
        String key = String.format("%s:%s:%s", refreshCachePrefix, username, deviceId);
        final byte[] stored = refreshTokenMap.getOrDefault(key, String.format("%64s", "0")).getBytes(StandardCharsets.UTF_8);
        final byte[] toCheck = token.getBytes(StandardCharsets.UTF_8);

        return tokenEquals(stored, toCheck)
                && refreshTokenMap.remove(key) != null;
    }

    @Override
    public List<RefreshToken> listTokens(String username) {
        String filter = String.format("^%s:%s:[^:]+$", Pattern.quote(refreshCachePrefix), Pattern.quote(username));
        return listRefreshTokensByPrefix(filter).getOrDefault(username, Collections.emptyList());
    }

    @Override
    public Map<String, List<RefreshToken>> listTokens() {
        String filter = String.format("^%s:[^:]+:[^:]+$", Pattern.quote(refreshCachePrefix));
        return listRefreshTokensByPrefix(filter);
    }

    private Map<String, List<RefreshToken>> listRefreshTokensByPrefix(String filter) {
        final Map<String, List<RefreshToken>> result = new HashMap<>();
        for (Map.Entry<String, String> entry : refreshTokenMap.entrySet()) {
            if (entry.getKey().matches(filter)) {
                String[] parts = entry.getKey().split(":", 3);
                String username = parts[1];
                String deviceId = parts[2];
                String token = entry.getValue();
                int expiresIn = (int) refreshTokenMap.getExpiration(entry.getKey());

                result.computeIfAbsent(username, s -> new ArrayList<>()).add(
                        new RefreshToken(token, expiresIn, deviceId)
                );
            }
        }
        result.replaceAll((s, t) -> Collections.unmodifiableList(t));
        return Collections.unmodifiableMap(result);
    }

    @Override
    public boolean revokeToken(String username, RefreshToken token) {
        return revokeTokens(Optional.of(username), Optional.of(token.getDeviceId())) != 0;
    }

    private int revokeTokens(Optional<String> username, Optional<String> deviceId) {
        if (!username.isPresent() && !deviceId.isPresent()) {
            final int count = refreshTokenMap.size();
            refreshTokenMap.clear();
            return count;
        }
        final String filter = String.format("%s:%s:%s",
                refreshCachePrefix,
                username.map(Pattern::quote).orElse("[^:]+"),
                deviceId.map(Pattern::quote).orElse("[^:]+"));
        int count = 0;
        for (Iterator<Map.Entry<String, String>> iterator = refreshTokenMap.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, String> entry = iterator.next();
            if (entry.getKey().matches(filter)) {
                iterator.remove();
                count++;
            }
        }

        return count;
    }

    @Override
    public int revokeTokens(String username) {
        return revokeTokens(Optional.of(username), Optional.empty());
    }

    @Override
    public boolean revokeToken(String username, String deviceId) {
        return revokeTokens(Optional.of(username), Optional.of(deviceId)) != 0;
    }

    @Override
    public int revokeTokens() {
        return revokeTokens(Optional.empty(), Optional.empty());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.debug("Creating in-memory expiring map");
        refreshTokenMap = ExpiringMap.builder()
                .expirationPolicy(ExpirationPolicy.CREATED)
                .expiration(refreshExpiration.getQuantity(), refreshExpiration.getTimeUnit())
                .build();
    }
}
