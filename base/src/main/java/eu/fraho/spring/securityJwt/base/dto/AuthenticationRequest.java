/*
 * MIT Licence
 * Copyright (c) 2026 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.Optional;

@com.fasterxml.jackson.databind.annotation.JsonDeserialize(builder = AuthenticationRequest.AuthenticationRequestBuilder.class)
@tools.jackson.databind.annotation.JsonDeserialize(builder = AuthenticationRequest.AuthenticationRequestBuilder.class)
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

    @com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "")
    @tools.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "")
    public static final class AuthenticationRequestBuilder {
    }
}
