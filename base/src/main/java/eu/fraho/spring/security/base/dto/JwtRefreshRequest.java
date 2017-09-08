/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.security.base.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.jcip.annotations.Immutable;

import javax.validation.constraints.NotNull;

@Immutable
@Getter
@AllArgsConstructor
public final class JwtRefreshRequest {
    @NotNull
    @JsonProperty(required = true)
    private String refreshToken;
}
