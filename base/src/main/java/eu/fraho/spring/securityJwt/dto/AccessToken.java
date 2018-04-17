/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.jcip.annotations.Immutable;

@JsonDeserialize(builder = AccessToken.AccessTokenBuilder.class)
@Value
@Immutable
@EqualsAndHashCode(callSuper = true)
public final class AccessToken extends AbstractToken {
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private final String type = "Bearer";

    @Builder
    public AccessToken(String token, long expiresIn) {
        super(token, expiresIn);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class AccessTokenBuilder {
    }
}
