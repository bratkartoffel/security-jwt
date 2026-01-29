/*
 * MIT Licence
 * Copyright (c) 2026 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.dataRedis.dto;

import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DataRedisEntry {
    private final Long id;
    private final String username;

    public static DataRedisEntry from(JwtUser user) {
        return DataRedisEntry.builder().id(user.getId()).username(user.getUsername()).build();
    }

    public static DataRedisEntry from(String entry) {
        String[] parts = entry.split(",", 2);
        return DataRedisEntry.builder().id(Long.valueOf(parts[0])).username(parts[1]).build();
    }

    public String toString() {
        return id + "," + username;
    }
}
