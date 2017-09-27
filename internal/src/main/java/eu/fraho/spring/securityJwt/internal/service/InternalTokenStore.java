/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.internal.service;

import eu.fraho.spring.securityJwt.config.RefreshProperties;
import eu.fraho.spring.securityJwt.dto.JwtUser;
import eu.fraho.spring.securityJwt.dto.RefreshToken;
import eu.fraho.spring.securityJwt.service.RefreshTokenStore;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.*;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class InternalTokenStore implements RefreshTokenStore {
    @NonNull
    private final RefreshProperties refreshProperties;

    @NonNull
    private UserDetailsService userDetailsService;

    //                  AbstractToken   User
    private ExpiringMap<String, JwtUser> refreshTokenMap;

    @Override
    public synchronized void saveToken(@NotNull JwtUser user, @NotNull String token) {
        refreshTokenMap.put(token, user);
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <T extends JwtUser> Optional<T> useToken(@NotNull String token) {
        return Optional.ofNullable(refreshTokenMap.remove(token))
                .map(JwtUser::getUsername)
                .map(userDetailsService::loadUserByUsername)
                .map(e -> (T) e);
    }

    @NotNull
    @Override
    public synchronized List<RefreshToken> listTokens(@NotNull JwtUser user) {
        return listTokens().getOrDefault(user.getId(), Collections.emptyList());
    }

    @NotNull
    @Override
    public synchronized Map<Long, List<RefreshToken>> listTokens() {
        final Map<Long, List<RefreshToken>> result = new HashMap<>();
        for (Map.Entry<String, JwtUser> entry : refreshTokenMap.entrySet()) {
            String token = entry.getKey();
            int expiresIn = (int) refreshTokenMap.getExpiration(entry.getKey());

            result.computeIfAbsent(entry.getValue().getId(), s -> new ArrayList<>()).add(
                    new RefreshToken(token, expiresIn)
            );
        }
        result.replaceAll((s, t) -> Collections.unmodifiableList(t));
        return Collections.unmodifiableMap(result);
    }

    @Override
    public synchronized boolean revokeToken(@NotNull String token) {
        return refreshTokenMap.remove(token) != null;
    }

    @Override
    public synchronized int revokeTokens(@NotNull JwtUser user) {
        return (int) listTokens(user).stream()
                .map(RefreshToken::getToken)
                .map(refreshTokenMap::remove)
                .filter(Objects::nonNull).count();
    }

    @Override
    public synchronized int revokeTokens() {
        int size = refreshTokenMap.size();
        refreshTokenMap.clear();
        return size;
    }

    @Override
    public void afterPropertiesSet() {
        log.info("Using in-memory implementation to handle refresh tokens");
        refreshTokenMap = ExpiringMap.builder()
                .expirationPolicy(ExpirationPolicy.CREATED)
                .expiration(refreshProperties.getExpiration().getQuantity(), refreshProperties.getExpiration().getTimeUnit())
                .build();
    }
}
