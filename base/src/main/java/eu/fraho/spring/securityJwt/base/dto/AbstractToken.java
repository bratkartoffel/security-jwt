/*
 * MIT Licence
 * Copyright (c) 2025 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode(of = {"token"})
public abstract class AbstractToken {
    @JsonProperty(required = true)
    @NonNull
    @Schema(description = "The access token")
    private final String token;

    @JsonProperty(required = true)
    @Schema(description = "Expiration timestamp")
    private final long expiresIn;
}
