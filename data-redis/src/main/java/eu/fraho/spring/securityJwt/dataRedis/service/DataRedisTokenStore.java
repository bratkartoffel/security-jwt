/*
 * MIT Licence
 * Copyright (c) 2026 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.dataRedis.service;

import eu.fraho.spring.securityJwt.base.config.RefreshProperties;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import eu.fraho.spring.securityJwt.base.dto.RefreshToken;
import eu.fraho.spring.securityJwt.base.dto.TimeWithPeriod;
import eu.fraho.spring.securityJwt.base.service.RefreshTokenStore;
import eu.fraho.spring.securityJwt.dataRedis.config.DataRedisProperties;
import eu.fraho.spring.securityJwt.dataRedis.dto.DataRedisEntry;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@Slf4j
@NoArgsConstructor
public class DataRedisTokenStore implements RefreshTokenStore {
    private RefreshProperties refreshProperties;

    private DataRedisProperties dataRedisProperties;

    private UserDetailsService userDetailsService;

    private StringRedisTemplate redisTemplate;

    @Override
    public void saveToken(JwtUser user, String token) {
        String key = dataRedisProperties.getPrefix() + token;
        String entry = DataRedisEntry.from(user).toString();
        TimeWithPeriod period = refreshProperties.getExpiration();
        redisTemplate.opsForValue().set(key, entry, period.getQuantity(), period.getTimeUnit());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends JwtUser> Optional<T> useToken(String token) {
        String key = dataRedisProperties.getPrefix() + token;
        return Optional.ofNullable(redisTemplate.opsForValue().getAndDelete(key)).map(entry -> {
            String username = DataRedisEntry.from(entry).getUsername();
            return (T) userDetailsService.loadUserByUsername(username);
        });
    }


    @Override
    public List<RefreshToken> listTokens(JwtUser user) {
        return listTokens().getOrDefault(user.getId(), Collections.emptyList());
    }

    @Override
    public Map<Long, List<RefreshToken>> listTokens() {
        final Map<Long, List<RefreshToken>> result = new HashMap<>();
        final int prefixLen = dataRedisProperties.getPrefix().length();

        Map<String, String> entries = listKeysWithValues();
        List<Map.Entry<Long, String>> meta = new ArrayList<>();

        List<Object> ttlResults = redisTemplate.executePipelined(
            (RedisCallback<Object>) connection -> {
                RedisKeyCommands redisKeyCommands = connection.keyCommands();
                for (Map.Entry<String, String> entry : entries.entrySet()) {
                    Long id = DataRedisEntry.from(entry.getValue()).getId();
                    String token = entry.getKey().substring(prefixLen);
                    meta.add(Map.entry(id, token));
                    redisKeyCommands.ttl(entry.getKey().getBytes(StandardCharsets.UTF_8));
                }
                return null;
            }
        );

        for (int i = 0; i < ttlResults.size(); i++) {
            Long id = meta.get(i).getKey();
            String token = meta.get(i).getValue();
            Long expiresIn = (Long) ttlResults.get(i);

            result.computeIfAbsent(id, k -> new ArrayList<>()).add(RefreshToken.builder()
                .token(token)
                .expiresIn(expiresIn)
                .build());
        }

        return Collections.unmodifiableMap(result);

    }

    @Override
    @SuppressWarnings("PointlessBooleanExpression")
    public boolean revokeToken(String token) {
        String key = dataRedisProperties.getPrefix() + token;
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    @Override
    public int revokeTokens(JwtUser user) {
        List<String> keys = new ArrayList<>(redisTemplate.keys(dataRedisProperties.getPrefix() + "*"));
        List<String> values = redisTemplate.opsForValue().multiGet(keys);

        List<String> deleteKeys = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++) {
            //noinspection DataFlowIssue
            if (values.get(i) == null) continue;
            DataRedisEntry dto = DataRedisEntry.from(values.get(i));
            if (Objects.equals(dto.getId(), user.getId())) {
                deleteKeys.add(keys.get(i));
            }
        }
        return redisTemplate.delete(deleteKeys).intValue();
    }

    @Override
    public int revokeTokens() {
        Set<String> keys = redisTemplate.keys(dataRedisProperties.getPrefix() + "*");
        return redisTemplate.delete(keys).intValue();
    }

    @Override
    public void afterPropertiesSet() {
        log.info("Using redis implementation to handle refresh tokens");
    }

    @Autowired
    public void setRefreshProperties(@NonNull RefreshProperties refreshProperties) {
        this.refreshProperties = refreshProperties;
    }

    @Autowired
    public void setDataRedisProperties(@NonNull DataRedisProperties dataRedisProperties) {
        this.dataRedisProperties = dataRedisProperties;
    }

    @Autowired
    public void setUserDetailsService(@NonNull UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    public void setStringRedisTemplate(@NonNull StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    protected <K, V> Map<K, V> zipToMap(List<K> keys, List<V> values) {
        Iterator<K> keyIter = keys.iterator();
        Iterator<V> valIter = values.iterator();
        Map<K, V> result = new HashMap<>();
        while (keyIter.hasNext() && valIter.hasNext()) {
            K key = keyIter.next();
            V val = valIter.next();

            if (key != null && val != null) {
                result.put(key, val);
            }
        }
        return result;
    }

    protected Map<String, String> listKeysWithValues() {
        List<String> keys = new ArrayList<>(redisTemplate.keys(dataRedisProperties.getPrefix() + "*"));
        if (keys.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> values = redisTemplate.opsForValue().multiGet(keys);

        //noinspection DataFlowIssue
        return zipToMap(keys, values);
    }
}
