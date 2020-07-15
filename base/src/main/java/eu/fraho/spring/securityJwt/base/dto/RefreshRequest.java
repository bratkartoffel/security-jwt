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
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@JsonDeserialize(builder = RefreshRequest.RefreshRequestBuilder.class)
@Value
@Builder
public class RefreshRequest {
    @JsonProperty(required = true)
    @NonNull
    @Schema(description = "Refresh token")
    String refreshToken;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class RefreshRequestBuilder {
    }
}
