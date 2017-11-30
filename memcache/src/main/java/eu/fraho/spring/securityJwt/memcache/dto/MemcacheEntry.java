/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.memcache.dto;

import eu.fraho.spring.securityJwt.dto.JwtUser;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Value
public class MemcacheEntry {
    private Long id;
    private String username;

    public static MemcacheEntry from(JwtUser user) {
        return new MemcacheEntry(user.getId(), user.getUsername());
    }

    public static MemcacheEntry from(String entry) {
        String[] parts = entry.split(",", 2);
        return new MemcacheEntry(Long.valueOf(parts[0]), parts[1]);
    }

    public String toString() {
        return id + "," + username;
    }
}
