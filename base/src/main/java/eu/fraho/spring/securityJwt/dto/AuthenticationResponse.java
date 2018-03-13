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
import lombok.NonNull;
import lombok.Value;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

@Immutable
@JsonDeserialize(builder = AuthenticationResponse.AuthenticationResponseBuilder.class)
@Builder
@Value
public final class AuthenticationResponse {
    @NotNull
    @NonNull
    @JsonProperty(required = true)
    private AccessToken accessToken;
    private RefreshToken refreshToken;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class AuthenticationResponseBuilder {
    }
}
