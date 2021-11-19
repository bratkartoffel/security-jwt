/*
 * MIT Licence
 * Copyright (c) 2021 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.internal.service;

import eu.fraho.spring.securityJwt.base.config.RefreshProperties;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import eu.fraho.spring.securityJwt.base.dto.RefreshToken;
import eu.fraho.spring.securityJwt.base.service.RefreshTokenStore;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@Slf4j
@NoArgsConstructor
public class InternalTokenStore implements RefreshTokenStore {
    private RefreshProperties refreshProperties;

    private UserDetailsService userDetailsService;

    //                  AbstractToken   User
    private ExpiringMap<String, JwtUser> refreshTokenMap;

    @Override
    public synchronized void saveToken(JwtUser user, String token) {
        refreshTokenMap.put(token, user);
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <T extends JwtUser> Optional<T> useToken(String token) {
        return Optional.ofNullable(refreshTokenMap.remove(token))
                .map(JwtUser::getUsername)
                .map(userDetailsService::loadUserByUsername)
                .map(e -> (T) e);
    }


    @Override
    public synchronized List<RefreshToken> listTokens(JwtUser user) {
        return listTokens().getOrDefault(user.getId(), Collections.emptyList());
    }


    @Override
    public synchronized Map<Long, List<RefreshToken>> listTokens() {
        final Map<Long, List<RefreshToken>> result = new HashMap<>();
        for (Map.Entry<String, JwtUser> entry : refreshTokenMap.entrySet()) {
            String token = entry.getKey();
            int expiresIn = (int) refreshTokenMap.getExpiration(entry.getKey());

            result.computeIfAbsent(entry.getValue().getId(), s -> new ArrayList<>()).add(
                    RefreshToken.builder().token(token).expiresIn(expiresIn).build()
            );
        }
        result.replaceAll((s, t) -> Collections.unmodifiableList(t));
        return Collections.unmodifiableMap(result);
    }

    @Override
    public synchronized boolean revokeToken(String token) {
        return refreshTokenMap.remove(token) != null;
    }

    @Override
    public synchronized int revokeTokens(JwtUser user) {
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

    @Autowired
    public void setRefreshProperties(@NonNull RefreshProperties refreshProperties) {
        this.refreshProperties = refreshProperties;
    }

    @Autowired
    public void setUserDetailsService(@NonNull UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    protected ExpiringMap<String, JwtUser> getRefreshTokenMap() {
        return refreshTokenMap;
    }

    protected void setRefreshTokenMap(ExpiringMap<String, JwtUser> refreshTokenMap) {
        this.refreshTokenMap = refreshTokenMap;
    }
}
