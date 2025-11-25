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

@com.fasterxml.jackson.databind.annotation.JsonDeserialize(builder = RefreshRequest.RefreshRequestBuilder.class)
@tools.jackson.databind.annotation.JsonDeserialize(builder = RefreshRequest.RefreshRequestBuilder.class)
@Value
@Builder
public class RefreshRequest {
    @JsonProperty(required = true)
    @NonNull
    @Schema(description = "Refresh token")
    String refreshToken;

    @com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "")
    @tools.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "")
    public static final class RefreshRequestBuilder {
    }
}
