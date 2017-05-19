package eu.fraho.spring.securityJwt.service;

import eu.fraho.spring.securityJwt.dto.RefreshToken;
import eu.fraho.spring.securityJwt.dto.TimeWithPeriod;
import eu.fraho.spring.securityJwt.exceptions.JwtRefreshException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.OperationFuture;
import org.springframework.beans.factory.annotation.Value;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static eu.fraho.spring.securityJwt.service.JwtTokenService.DEFAULT_CACHE_PREFIX;
import static eu.fraho.spring.securityJwt.service.JwtTokenService.DEFAULT_REFRESH_EXPIRATION;

/**
 * <h3>Used properties from configuration file:</h3>
 * <table border="1" summary="list of configuration properties">
 * <tr>
 * <th>Property</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>fraho.jwt.refresh.cache.memcache.host</td>
 * <td>{@value #DEFAULT_MEMCACHE_HOST}</td>
 * <td>Hostname or IP Adress of memcache server.</td>
 * </tr>
 * <tr>
 * <td>fraho.jwt.refresh.cache.memcache.port</td>
 * <td>{@value #DEFAULT_MEMCACHE_PORT}</td>
 * <td>Port of memcache server.</td>
 * </tr>
 * <tr>
 * <td>fraho.jwt.refresh.cache.memcache.timeout</td>
 * <td>{@value #DEFAULT_MEMCACHE_TIMEOUT}</td>
 * <td>Timeout (in seconds) when talking to memcache server.</td>
 * </tr>
 * </table>
 */
@Slf4j
@Getter
public class MemcacheTokenStore implements RefreshTokenStore {
    public static final String DEFAULT_MEMCACHE_HOST = "127.0.0.1";
    public static final int DEFAULT_MEMCACHE_PORT = 11211;
    public static final int DEFAULT_MEMCACHE_TIMEOUT = 5;

    @Value("${fraho.jwt.refresh.expiration:" + DEFAULT_REFRESH_EXPIRATION + "}")
    private TimeWithPeriod refreshExpiration = new TimeWithPeriod(DEFAULT_REFRESH_EXPIRATION);
    @Value("${fraho.jwt.refresh.cache.prefix:" + DEFAULT_CACHE_PREFIX + "}")
    private String refreshCachePrefix = DEFAULT_CACHE_PREFIX;
    @Value("${fraho.jwt.refresh.cache.memcache.host:" + DEFAULT_MEMCACHE_HOST + "}")
    private String cacheHost = DEFAULT_MEMCACHE_HOST;
    @Value("${fraho.jwt.refresh.cache.memcache.port:" + DEFAULT_MEMCACHE_PORT + "}")
    private Integer cachePort = DEFAULT_MEMCACHE_PORT;
    @Value("${fraho.jwt.refresh.cache.memcache.timeout:" + DEFAULT_MEMCACHE_TIMEOUT + "}")
    private Integer cacheTimeout = DEFAULT_MEMCACHE_TIMEOUT;

    private MemcachedClient memcachedClient = null;

    @Override
    public void saveToken(String username, String deviceId, String token) {
        String key = String.format("%s:%s:%s", refreshCachePrefix, username, deviceId);
        try {
            memcachedClient.set(key, refreshExpiration.toSeconds(), token).get(cacheTimeout, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            throw new JwtRefreshException("Error while saving refresh token on memcache server", e);
        }
    }

    @Override
    public boolean useToken(String username, String deviceId, String token) {
        String key = String.format("%s:%s:%s", refreshCachePrefix, username, deviceId);
        final byte[] stored = String.valueOf(memcachedClient.get(key)).getBytes(StandardCharsets.UTF_8);
        final byte[] toCheck = token.getBytes(StandardCharsets.UTF_8);

        if (tokenEquals(stored, toCheck)) {
            try {
                return memcachedClient.delete(key).get(cacheTimeout, TimeUnit.SECONDS);
            } catch (TimeoutException | InterruptedException | ExecutionException e) {
                throw new JwtRefreshException("Error while deleting refresh token on memcache server", e);
            }
        }

        return false;
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

    private List<String> listAllKeys(String filter) {
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

    private Map<String, List<RefreshToken>> listRefreshTokensByPrefix(String filter) {
        final Map<String, List<RefreshToken>> result = new HashMap<>();
        final List<String> keys = listAllKeys(filter);
        final Map<String, Object> entries = memcachedClient.getBulk(keys);
        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            if (entry.getKey().matches(filter)) {
                String[] parts = entry.getKey().split(":", 3);
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
    public boolean revokeToken(String username, RefreshToken token) {
        return revokeTokens(Optional.of(username), Optional.of(token.getDeviceId())) != 0;
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

    private int revokeTokens(Optional<String> username, Optional<String> deviceId) {
        final String filter = String.format("%s:%s:%s",
                refreshCachePrefix,
                username.map(Pattern::quote).orElse("[^:]+"),
                deviceId.map(Pattern::quote).orElse("[^:]+"));

        final List<String> keys = listAllKeys(filter);
        final List<OperationFuture> futures = new ArrayList<>();
        for (String key : keys) {
            futures.add(memcachedClient.delete(key));
        }

        int count = keys.size();
        for (OperationFuture future : futures) {
            try {
                future.get(cacheTimeout, TimeUnit.SECONDS);
            } catch (TimeoutException | InterruptedException | ExecutionException e) {
                log.error("Error while deleting refresh token on memcache server", e);
                count--;
            }
        }

        return count;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (refreshExpiration.toSeconds() > 2_592_000) {
            throw new IllegalStateException("Refresh expiration may not exceed 30 days when using memcached");
        }

        log.info("Starting memcache connection to {}:{}", cacheHost, cachePort);
        memcachedClient = new MemcachedClient(new InetSocketAddress(cacheHost, cachePort));
    }
}
