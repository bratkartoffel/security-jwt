/*
 * MIT Licence
 * Copyright (c) 2026 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.redis.service;

import eu.fraho.spring.securityJwt.base.config.RefreshProperties;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import eu.fraho.spring.securityJwt.base.dto.RefreshToken;
import eu.fraho.spring.securityJwt.base.service.RefreshTokenStore;
import eu.fraho.spring.securityJwt.redis.config.RedisProperties;
import eu.fraho.spring.securityJwt.redis.dto.RedisEntry;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import redis.clients.jedis.AbstractTransaction;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@Slf4j
@NoArgsConstructor
public class RedisTokenStore implements RefreshTokenStore {
    private RefreshProperties refreshProperties;

    private RedisProperties redisProperties;

    private UserDetailsService userDetailsService;

    private RedisClient client;

    @Override
    public void saveToken(JwtUser user, String token) {
        String key = redisProperties.getPrefix() + token;
        String entry = RedisEntry.from(user).toString();
        try (AbstractTransaction t = client.multi()) {
            t.set(key, entry);
            t.pexpire(key, refreshProperties.getExpiration().toMillis());
            t.exec();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends JwtUser> Optional<T> useToken(String token) {
        String key = redisProperties.getPrefix() + token;
        Optional<T> result = Optional.empty();
        try (AbstractTransaction transaction = client.multi()) {
            Response<String> tmp = transaction.get(key);
            Response<Long> del = transaction.del(key);
            transaction.exec();
            String found = tmp.get();
            if (found != null && del.get() == 1) {
                String username = RedisEntry.from(found).getUsername();
                result = Optional.ofNullable((T) userDetailsService.loadUserByUsername(username));
            }
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
        final int prefixLen = redisProperties.getPrefix().length();
        Map<String, String> entries = listKeysWithValues();
        List<Runnable> resultBuilder = new ArrayList<>();
        try (Pipeline p = client.pipelined()) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                Long id = RedisEntry.from(entry.getValue()).getId();
                String token = entry.getKey().substring(prefixLen);
                List<RefreshToken> tokenList = result.computeIfAbsent(id, s -> new ArrayList<>());
                Response<Long> expiresIn = p.ttl(entry.getKey());
                resultBuilder.add(() -> tokenList.add(
                                RefreshToken.builder()
                                        .token(token)
                                        .expiresIn(expiresIn.get())
                                        .build()
                        )
                );
            }
            p.sync();
            resultBuilder.forEach(Runnable::run);
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public boolean revokeToken(String token) {
        String key = redisProperties.getPrefix() + token;
        return client.del(key) == 1L;
    }

    @Override
    public int revokeTokens(JwtUser user) {
        List<String> keys = new ArrayList<>(client.keys(redisProperties.getPrefix() + "*"));
        List<String> values = client.mget(keys.toArray(new String[0]));

        long counter = 0;
        for (int i = 0; i < keys.size(); i++) {
            if (values.get(i) == null) continue;
            RedisEntry dto = RedisEntry.from(values.get(i));
            if (Objects.equals(dto.getId(), user.getId())) {
                counter += client.del(keys.get(i));
            }
        }
        return (int) counter;
    }

    @Override
    public int revokeTokens() {
        List<String> keys = new ArrayList<>(client.keys(redisProperties.getPrefix() + "*"));
        try (AbstractTransaction transaction = client.multi()) {
            keys.forEach(transaction::del);
            return transaction.exec().size();
        }
    }

    @Override
    public void afterPropertiesSet() {
        log.info("Using redis implementation to handle refresh tokens");
        log.info("Starting redis connection pool to {}:{}", redisProperties.getHost(), redisProperties.getPort());

        client = RedisClient.builder().hostAndPort(redisProperties.getHost(), redisProperties.getPort())
                .clientConfig(DefaultJedisClientConfig.builder().build())
                .poolConfig(redisProperties.getPoolConfig())
                .build();
    }

    @Autowired
    public void setRefreshProperties(@NonNull RefreshProperties refreshProperties) {
        this.refreshProperties = refreshProperties;
    }

    @Autowired
    public void setRedisProperties(@NonNull RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Autowired
    public void setUserDetailsService(@NonNull UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
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
        List<String> keys = new ArrayList<>(client.keys(redisProperties.getPrefix() + "*"));
        if (keys.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> values = client.mget(keys.toArray(new String[0]));
        return zipToMap(keys, values);
    }
}
