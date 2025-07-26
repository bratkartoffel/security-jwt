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

import java.util.Optional;

@JsonDeserialize(builder = AuthenticationRequest.AuthenticationRequestBuilder.class)
@Value
@Builder
public class AuthenticationRequest {
    @JsonProperty(required = true)
    @NonNull
    @Schema(description = "Authentication username")
    String username;

    @JsonProperty(required = true)
    @NonNull
    @Schema(description = "Authentication password")
    String password;

    @Schema(description = "OTP code")
    Integer totp;

    public Optional<Integer> getTotp() {
        return Optional.ofNullable(totp);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class AuthenticationRequestBuilder {
    }
}
