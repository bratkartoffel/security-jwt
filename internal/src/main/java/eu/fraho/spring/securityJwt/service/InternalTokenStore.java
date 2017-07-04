/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.service;

import eu.fraho.spring.securityJwt.dto.RefreshToken;
import eu.fraho.spring.securityJwt.dto.TimeWithPeriod;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
public class InternalTokenStore implements RefreshTokenStore {
    @Value("${fraho.jwt.refresh.expiration:" + JwtTokenServiceImpl.DEFAULT_REFRESH_EXPIRATION + "}")
    private TimeWithPeriod refreshExpiration = new TimeWithPeriod(JwtTokenServiceImpl.DEFAULT_REFRESH_EXPIRATION);

    @Value("${fraho.jwt.refresh.delimiter:" + JwtTokenServiceImpl.DEFAULT_DELIMITER + "}")
    private String delimiter = JwtTokenServiceImpl.DEFAULT_DELIMITER;

    private ExpiringMap<String, String> refreshTokenMap = null;

    @Override
    public void saveToken(@NotNull String username, @NotNull String deviceId, @NotNull String token) {
        String key = username + delimiter + deviceId;
        refreshTokenMap.put(key, token);
    }

    @Override
    public boolean useToken(@NotNull String username, @NotNull String deviceId, @NotNull String token) {
        String key = username + delimiter + deviceId;
        final byte[] stored = refreshTokenMap.getOrDefault(key, String.format("%64s", "0")).getBytes(StandardCharsets.UTF_8);
        final byte[] toCheck = token.getBytes(StandardCharsets.UTF_8);

        return tokenEquals(stored, toCheck)
                && refreshTokenMap.remove(key) != null;
    }

    @NotNull
    @Override
    public List<RefreshToken> listTokens(@NotNull String username) {
        String filter = String.format("^%s%s[^%s]+$",
                Pattern.quote(username), Pattern.quote(delimiter), Pattern.quote(delimiter));
        return listRefreshTokensByPrefix(filter).getOrDefault(username, Collections.emptyList());
    }

    @NotNull
    @Override
    public Map<String, List<RefreshToken>> listTokens() {
        final String filter = String.format("^[^%s]+%s[^%s]+$",
                Pattern.quote(delimiter), Pattern.quote(delimiter), Pattern.quote(delimiter));
        return listRefreshTokensByPrefix(filter);
    }

    @NotNull
    private Map<String, List<RefreshToken>> listRefreshTokensByPrefix(@NotNull String filter) {
        final Map<String, List<RefreshToken>> result = new HashMap<>();
        for (Map.Entry<String, String> entry : refreshTokenMap.entrySet()) {
            if (entry.getKey().matches(filter)) {
                String[] parts = entry.getKey().split(Pattern.quote(delimiter), 2);
                String username = parts[0];
                String deviceId = parts[1];
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
    public boolean revokeToken(@NotNull String username, @NotNull RefreshToken token) {
        Objects.requireNonNull(username, "username may not be null");
        Objects.requireNonNull(token, "token may not be null");
        return revokeTokens(username, token.getDeviceId()) != 0;
    }

    private int revokeTokens(@Nullable String username, @Nullable String deviceId) {
        if (username == null && deviceId == null) {
            final int count = refreshTokenMap.size();
            refreshTokenMap.clear();
            return count;
        }

        final String filter = Optional.ofNullable(username).map(Pattern::quote).orElse("[^" + Pattern.quote(delimiter) + "]+") +
                Pattern.quote(delimiter) +
                Optional.ofNullable(deviceId).map(Pattern::quote).orElse("[^" + Pattern.quote(delimiter) + "]+");
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
    public int revokeTokens(@NotNull String username) {
        Objects.requireNonNull(username, "username may not be null");
        return revokeTokens(username, null);
    }

    @Override
    public boolean revokeToken(@NotNull String username, @NotNull String deviceId) {
        Objects.requireNonNull(username, "username may not be null");
        Objects.requireNonNull(deviceId, "deviceId may not be null");
        return revokeTokens(username, deviceId) != 0;
    }

    @Override
    public int revokeTokens() {
        return revokeTokens(null, null);
    }

    @Override
    public void afterPropertiesSet() {
        log.debug("Creating in-memory expiring map");
        refreshTokenMap = ExpiringMap.builder()
                .expirationPolicy(ExpirationPolicy.CREATED)
                .expiration(refreshExpiration.getQuantity(), refreshExpiration.getTimeUnit())
                .build();
    }

    @NotNull
    @Override
    public TimeWithPeriod getRefreshExpiration() {
        return refreshExpiration;
    }
}
