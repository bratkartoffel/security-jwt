/*
 * MIT Licence
 * Copyright (c) 2021 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@JsonDeserialize(builder = RefreshToken.RefreshTokenBuilder.class)
@Value
@EqualsAndHashCode(callSuper = true)
public class RefreshToken extends AbstractToken {
    @Builder
    public RefreshToken(String token, long expiresIn) {
        super(token, expiresIn);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class RefreshTokenBuilder {
    }
}
