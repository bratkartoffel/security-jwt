/*
 * MIT Licence
 * Copyright (c) 2025 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@com.fasterxml.jackson.databind.annotation.JsonDeserialize(builder = AuthenticationResponse.AuthenticationResponseBuilder.class)
@tools.jackson.databind.annotation.JsonDeserialize(builder = AuthenticationResponse.AuthenticationResponseBuilder.class)
@Value
@Builder
public class AuthenticationResponse {
    @JsonProperty(required = true)
    @NonNull
    @Schema(description = "Access token")
    AccessToken accessToken;

    @Schema(description = "Refresh token")
    RefreshToken refreshToken;

    @com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "")
    @tools.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "")
    public static final class AuthenticationResponseBuilder {
    }
}
