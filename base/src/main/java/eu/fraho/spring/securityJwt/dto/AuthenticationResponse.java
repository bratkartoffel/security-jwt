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
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = AuthenticationResponse.AuthenticationResponseBuilder.class)
@Builder
@Immutable
@Value
public final class AuthenticationResponse {
    @JsonProperty(required = true)
    @NotNull
    @NonNull
    private final AccessToken accessToken;

    @Nullable
    private final RefreshToken refreshToken;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class AuthenticationResponseBuilder {
    }
}
