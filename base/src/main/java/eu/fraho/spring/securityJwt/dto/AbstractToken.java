/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

@Getter
@Immutable
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"expiresIn"})
public abstract class AbstractToken {
    @JsonProperty(required = true)
    @NotNull
    @NonNull
    private final String token;

    @JsonProperty(required = true)
    private final int expiresIn;
}
