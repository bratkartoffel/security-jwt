/*
 * MIT Licence
 * Copyright (c) 2020 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@JsonDeserialize(builder = AccessToken.AccessTokenBuilder.class)
@Value
@EqualsAndHashCode(callSuper = true)
public class AccessToken extends AbstractToken {
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String type = "Bearer";

    @Builder
    public AccessToken(String token, long expiresIn) {
        super(token, expiresIn);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class AccessTokenBuilder {
    }
}
