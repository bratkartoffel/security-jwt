/*
 * MIT Licence
 * Copyright (c) 2025 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@JsonDeserialize(builder = AuthenticationResponse.AuthenticationResponseBuilder.class)
@Value
@Builder
public class AuthenticationResponse {
    @JsonProperty(required = true)
    @NonNull
    @Schema(description = "Access token")
    AccessToken accessToken;

    @Schema(description = "Refresh token")
    RefreshToken refreshToken;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class AuthenticationResponseBuilder {
    }
}
