/*
 * MIT Licence
 * Copyright (c) 2025 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@com.fasterxml.jackson.databind.annotation.JsonDeserialize(builder = RefreshToken.RefreshTokenBuilder.class)
@tools.jackson.databind.annotation.JsonDeserialize(builder = RefreshToken.RefreshTokenBuilder.class)
@Value
@EqualsAndHashCode(callSuper = true)
public class RefreshToken extends AbstractToken {
    @Builder
    public RefreshToken(String token, long expiresIn) {
        super(token, expiresIn);
    }

    @com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "")
    @tools.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "")
    public static final class RefreshTokenBuilder {
    }
}
