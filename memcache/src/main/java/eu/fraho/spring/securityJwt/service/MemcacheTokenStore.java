/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.service;

import eu.fraho.spring.securityJwt.config.JwtRefreshConfiguration;
import eu.fraho.spring.securityJwt.config.MemcacheConfiguration;
import eu.fraho.spring.securityJwt.dto.RefreshToken;
import eu.fraho.spring.securityJwt.dto.TimeWithPeriod;
import eu.fraho.spring.securityJwt.exceptions.JwtRefreshException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.OperationFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MemcacheTokenStore implements RefreshTokenStore {
    @NonNull
    private final JwtRefreshConfiguration refreshConfig;

    @NonNull
    private final MemcacheConfiguration memcacheConfig;

    private MemcachedClient memcachedClient = null;

    private <T> T getAndWait(@NotNull String message, @NotNull Supplier<OperationFuture<T>> action) {
        try {
            return action.get().get(memcacheConfig.getTimeout(), TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            throw new JwtRefreshException(message, e);
        }
    }

    @Override
    public void saveToken(@NotNull String username, @NotNull String deviceId, @NotNull String token) {
        String key = memcacheConfig.getPrefix() + refreshConfig.getDelimiter() + username + refreshConfig.getDelimiter() + deviceId;
        getAndWait("Error while saving refresh token on memcache server", () ->
                memcachedClient.set(key, refreshConfig.getExpiration().toSeconds(), token));
    }

    @Override
    public boolean useToken(@NotNull String username, @NotNull String deviceId, @NotNull String token) {
        String key = memcacheConfig.getPrefix() + refreshConfig.getDelimiter() + username + refreshConfig.getDelimiter() + deviceId;
        final byte[] stored = String.valueOf(memcachedClient.get(key)).getBytes(StandardCharsets.UTF_8);
        final byte[] toCheck = token.getBytes(StandardCharsets.UTF_8);

        if (tokenEquals(stored, toCheck)) {
            return getAndWait("Error while deleting refresh token on memcache server", () ->
                    memcachedClient.delete(key));
        }

        return false;
    }

    @NotNull
    @Override
    public List<RefreshToken> listTokens(@NotNull String username) {
        String filter = String.format("^%s%s%s%s[^%s]+$",
                Pattern.quote(memcacheConfig.getPrefix()), Pattern.quote(refreshConfig.getDelimiter()),
                Pattern.quote(username), Pattern.quote(refreshConfig.getDelimiter()), Pattern.quote(refreshConfig.getDelimiter()));
        return listRefreshTokensByPrefix(filter).getOrDefault(username, Collections.emptyList());
    }

    @NotNull
    @Override
    public Map<String, List<RefreshToken>> listTokens() {
        String filter = String.format("^%s%s[^%s]+%s[^%s]+$",
                Pattern.quote(memcacheConfig.getPrefix()), Pattern.quote(refreshConfig.getDelimiter()), Pattern.quote(refreshConfig.getDelimiter()),
                Pattern.quote(refreshConfig.getDelimiter()), Pattern.quote(refreshConfig.getDelimiter()));
        return listRefreshTokensByPrefix(filter);
    }

    @NotNull
    private List<String> listAllKeys(@NotNull String filter) {
        final List<String> result = new ArrayList<>();
        final Set<Integer> slabs = memcachedClient.getStats("items")
                .entrySet().iterator().next()
                .getValue().keySet().stream()
                .filter(k -> k.matches("^items:[0-9]+:number$"))
                .map(s -> s.split(":")[1])
                .map(Integer::valueOf)
                .collect(Collectors.toSet());

        for (Integer slab : slabs) {
            int used_chunks = Integer.valueOf(
                    memcachedClient.getStats("slabs " + slab)
                            .entrySet().iterator().next()
                            .getValue().get(slab + ":used_chunks"));

            final Set<String> entries = memcachedClient.getStats("cachedump " + slab + " " + used_chunks)
                    .entrySet().iterator().next().getValue().keySet();

            entries.stream().filter(e -> e.matches(filter)).forEach(result::add);
        }
        return Collections.unmodifiableList(result);
    }

    @NotNull
    private Map<String, List<RefreshToken>> listRefreshTokensByPrefix(@NotNull String filter) {
        final Map<String, List<RefreshToken>> result = new HashMap<>();
        final List<String> keys = listAllKeys(filter);
        final Map<String, Object> entries = memcachedClient.getBulk(keys);
        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            if (entry.getKey().matches(filter)) {
                String[] parts = entry.getKey().split(Pattern.quote(refreshConfig.getDelimiter()), 3);
                String username = parts[1];
                String deviceId = parts[2];
                String token = String.valueOf(entry.getValue());
                int expiresIn = -1;

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
        return revokeTokens(username, token.getDeviceId()) != 0;
    }

    @Override
    public int revokeTokens(@NotNull String username) {
        return revokeTokens(username, null);
    }

    @Override
    public boolean revokeToken(@NotNull String username, @NotNull String deviceId) {
        return revokeTokens(username, deviceId) != 0;
    }

    @Override
    public int revokeTokens() {
        return revokeTokens(null, null);
    }

    private int revokeTokens(@Nullable String username, @Nullable String deviceId) {
        final String filter = memcacheConfig.getPrefix() +
                Pattern.quote(refreshConfig.getDelimiter()) +
                Optional.ofNullable(username).map(Pattern::quote).orElse("[^" + Pattern.quote(refreshConfig.getDelimiter()) + "]+") +
                Pattern.quote(refreshConfig.getDelimiter()) +
                Optional.ofNullable(deviceId).map(Pattern::quote).orElse("[^" + Pattern.quote(refreshConfig.getDelimiter()) + "]+");

        final List<String> keys = listAllKeys(filter);
        final List<OperationFuture<Boolean>> futures = new ArrayList<>();
        for (String key : keys) {
            futures.add(memcachedClient.delete(key));
        }

        int count = keys.size();
        for (OperationFuture<Boolean> future : futures) {
            if (!getAndWait("Error while saving refresh token on memcache server", () -> future)) {
                count--;
            }
        }

        return count;
    }

    @Override
    public void afterPropertiesSet() throws IOException {
        log.info("Using memcache implementation to handle refresh tokens");
        if (refreshConfig.getExpiration().toSeconds() > 2_592_000) {
            throw new IllegalStateException("Refresh expiration may not exceed 30 days when using memcached");
        }

        log.info("Starting memcache connection to {}:{}", memcacheConfig.getHost(), memcacheConfig.getPort());
        memcachedClient = new MemcachedClient(new InetSocketAddress(memcacheConfig.getHost(), memcacheConfig.getPort()));
    }

    @NotNull
    @Override
    public TimeWithPeriod getRefreshExpiration() {
        return refreshConfig.getExpiration();
    }

    @NotNull
    MemcachedClient getMemcachedClient() {
        return memcachedClient;
    }
}
