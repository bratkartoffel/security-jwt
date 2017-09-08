/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.security.base.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.jcip.annotations.Immutable;

@Getter
@Immutable
@EqualsAndHashCode(callSuper = true)
public final class RefreshToken extends Token {
    @JsonCreator
    public RefreshToken(@JsonProperty("token") String token, @JsonProperty("expiresIn") int expiresIn) {
        super(token, expiresIn);
    }
}
