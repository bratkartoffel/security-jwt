/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@JsonDeserialize(builder = RefreshToken.RefreshTokenBuilder.class)
@Value
@EqualsAndHashCode(callSuper = true)
public final class RefreshToken extends AbstractToken {
    @Builder
    public RefreshToken(String token, int expiresIn) {
        super(token, expiresIn);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class RefreshTokenBuilder {
    }
}
