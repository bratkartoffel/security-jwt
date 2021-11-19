/*
 * MIT Licence
 * Copyright (c) 2021 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.redis.dto;

import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.jcip.annotations.Immutable;

@Getter
@Immutable
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisEntry {
    private final Long id;
    private final String username;

    public static RedisEntry from(JwtUser user) {
        return RedisEntry.builder().id(user.getId()).username(user.getUsername()).build();
    }

    public static RedisEntry from(String entry) {
        String[] parts = entry.split(",", 2);
        return RedisEntry.builder().id(Long.valueOf(parts[0])).username(parts[1]).build();
    }

    public String toString() {
        return id + "," + username;
    }
}
