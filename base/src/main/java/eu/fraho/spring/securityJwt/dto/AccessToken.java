/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.jcip.annotations.Immutable;

@Getter
@Immutable
@EqualsAndHashCode(callSuper = true)
public final class AccessToken extends Token {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private final String type = "Bearer";

    @JsonCreator
    public AccessToken(@JsonProperty("token") String token, @JsonProperty("expiresIn") int expiresIn) {
        super(token, expiresIn);
    }
}
