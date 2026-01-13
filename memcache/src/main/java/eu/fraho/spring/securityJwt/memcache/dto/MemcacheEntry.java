/*
 * MIT Licence
 * Copyright (c) 2026 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.memcache.dto;

import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemcacheEntry {
    private final Long id;
    private final String username;

    public static MemcacheEntry from(JwtUser user) {
        return MemcacheEntry.builder().id(user.getId()).username(user.getUsername()).build();
    }

    public static MemcacheEntry from(String entry) {
        String[] parts = entry.split(",", 2);
        return MemcacheEntry.builder().id(Long.valueOf(parts[0])).username(parts[1]).build();
    }

    public String toString() {
        return id + "," + username;
    }
}
