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
import lombok.Getter;
import lombok.NonNull;
import net.jcip.annotations.Immutable;

@JsonDeserialize(builder = AuthenticationResponse.AuthenticationResponseBuilder.class)
@Getter
@Builder
@Immutable
public final class AuthenticationResponse {
    @JsonProperty(required = true)

    @NonNull
    private final AccessToken accessToken;

    private final RefreshToken refreshToken;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class AuthenticationResponseBuilder {
    }
}
