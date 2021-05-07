/*
 * MIT Licence
 * Copyright (c) 2021 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.memcache.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LruMetadumpEntry {
    String key;
    Integer exp;
    Integer la;
    Integer cas;
    Boolean fetch;
    Integer cls;
    Integer size;
}
