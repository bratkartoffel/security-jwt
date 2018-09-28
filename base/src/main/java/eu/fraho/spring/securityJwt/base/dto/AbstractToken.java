/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import net.jcip.annotations.Immutable;

@Getter
@Immutable
@AllArgsConstructor
@EqualsAndHashCode(of = {"token"})
public abstract class AbstractToken {
    @JsonProperty(required = true)

    @NonNull
    private final String token;

    @JsonProperty(required = true)
    private final long expiresIn;
}
