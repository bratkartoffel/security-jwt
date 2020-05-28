/*
 * MIT Licence
 * Copyright (c) 2020 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.memcache.service;

import eu.fraho.spring.securityJwt.base.config.RefreshProperties;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import eu.fraho.spring.securityJwt.base.dto.RefreshToken;
import eu.fraho.spring.securityJwt.base.exceptions.RefreshException;
import eu.fraho.spring.securityJwt.base.service.RefreshTokenStore;
import eu.fraho.spring.securityJwt.memcache.config.MemcacheProperties;
import eu.fraho.spring.securityJwt.memcache.dto.MemcacheEntry;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.OperationFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@Slf4j
@NoArgsConstructor
public class MemcacheTokenStore implements RefreshTokenStore {
    private RefreshProperties refreshProperties;

    private MemcacheProperties memcacheProperties;

    private UserDetailsService userDetailsService;

    private MemcachedClient memcachedClient;

    private OperationFuture<Boolean> getAndWait(String message, Supplier<OperationFuture<Boolean>> action) {
        try {
            OperationFuture<Boolean> future = action.get();
            future.get(memcacheProperties.getTimeout(), TimeUnit.SECONDS);
            return future;
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            throw new RefreshException(message, e);
        }
    }

    @Override
    public void saveToken(JwtUser user, String token) {
        String key = memcacheProperties.getPrefix() + token;
        String entry = MemcacheEntry.from(user).toString();
        OperationFuture<Boolean> future = getAndWait("Error while saving refresh token on memcache server", () ->
                memcachedClient.set(key, (int) refreshProperties.getExpiration().toSeconds(), entry)
        );
        if (!future.getStatus().isSuccess()) {
            throw new RefreshException("Could not save the token, memcached responded with '!Status.isSuccess()': " + future.getStatus().getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends JwtUser> Optional<T> useToken(String token) {
        String key = memcacheProperties.getPrefix() + token;
        // will be "null" if invalid token
        final Object found = memcachedClient.get(key);

        Optional<T> result = Optional.empty();
        if (found != null) {
            String username = MemcacheEntry.from((String) found).getUsername();
            result = Optional.ofNullable((T) userDetailsService.loadUserByUsername(username));
        }
        if (!getAndWait("Error while deleting refresh token on memcache server", () -> memcachedClient.delete(key)).getStatus().isSuccess()) {
            result = Optional.empty();
        }

        return result;
    }


    @Override
    public List<RefreshToken> listTokens(JwtUser user) {
        return listTokens().getOrDefault(user.getId(), Collections.emptyList());
    }


    @Override
    public Map<Long, List<RefreshToken>> listTokens() {
        final Map<Long, List<RefreshToken>> result = new HashMap<>();
        final List<String> keys = listAllKeys();
        final Map<String, Object> entries = memcachedClient.getBulk(keys);
        final int prefixLen = memcacheProperties.getPrefix().length();
        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            MemcacheEntry dto = MemcacheEntry.from((String) entry.getValue());
            int expiresIn = -1;
            String token = entry.getKey().substring(prefixLen);
            result.computeIfAbsent(dto.getId(), s -> new ArrayList<>()).add(
                    RefreshToken.builder()
                            .token(token)
                            .expiresIn(expiresIn).build()
            );
        }

        result.replaceAll((s, t) -> Collections.unmodifiableList(t));
        return Collections.unmodifiableMap(result);
    }

    @Override
    public boolean revokeToken(String token) {
        String key = memcacheProperties.getPrefix() + token;
        return getAndWait("Error while deleting refresh token on memcache server", () ->
                memcachedClient.delete(key)).getStatus().isSuccess();
    }

    @Override
    public int revokeTokens(JwtUser user) {
        List<String> allKeys = listAllKeys();
        final Map<String, Object> entries = memcachedClient.getBulk(allKeys);

        final List<OperationFuture<Boolean>> futures = new ArrayList<>();
        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            MemcacheEntry dto = MemcacheEntry.from((String) entry.getValue());
            if (Objects.equals(dto.getId(), user.getId())) {
                futures.add(memcachedClient.delete(entry.getKey()));
            }
        }

        return submitAndCountSuccess(allKeys, futures);
    }


    private List<String> listAllKeys() {
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
                    .entrySet().iterator().next().getValue().keySet()
                    .stream().filter(e -> e.startsWith(memcacheProperties.getPrefix()))
                    .collect(Collectors.toSet());

            result.addAll(entries);
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public int revokeTokens() {
        final List<String> keys = listAllKeys();
        final List<OperationFuture<Boolean>> futures = new ArrayList<>();
        for (String key : keys) {
            futures.add(memcachedClient.delete(key));
        }

        return submitAndCountSuccess(keys, futures);
    }

    private int submitAndCountSuccess(List<String> keys, List<OperationFuture<Boolean>> futures) {
        int count = keys.size();
        for (OperationFuture<Boolean> future : futures) {
            if (!getAndWait("Error while saving refresh token on memcache server", () -> future).getStatus().isSuccess()) {
                count--;
            }
        }
        return count;
    }

    @Override
    public void afterPropertiesSet() throws IOException {
        log.info("Using memcache implementation to handle refresh tokens");
        if (refreshProperties.getExpiration().toSeconds() > 2_592_000) {
            throw new IllegalStateException("Refresh expiration may not exceed 30 days when using memcached");
        }

        log.info("Starting memcache connection to {}:{}", memcacheProperties.getHost(), memcacheProperties.getPort());
        memcachedClient = new MemcachedClient(new InetSocketAddress(memcacheProperties.getHost(), memcacheProperties.getPort()));
    }

    @Autowired
    public void setRefreshProperties(@NonNull RefreshProperties refreshProperties) {
        this.refreshProperties = refreshProperties;
    }

    @Autowired
    public void setMemcacheProperties(@NonNull MemcacheProperties memcacheProperties) {
        this.memcacheProperties = memcacheProperties;
    }

    @Autowired
    public void setUserDetailsService(@NonNull UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
}
