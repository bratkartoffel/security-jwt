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

import java.util.Optional;

@JsonDeserialize(builder = AuthenticationRequest.AuthenticationRequestBuilder.class)
@Value
@Immutable
@Builder
public final class AuthenticationRequest {
    @JsonProperty(required = true)
    @NotNull
    @NonNull
    private final String username;

    @JsonProperty(required = true)
    @NotNull
    @NonNull
    private final String password;

    @Nullable
    private final Integer totp;

    public Optional<Integer> getTotp() {
        return Optional.ofNullable(totp);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class AuthenticationRequestBuilder {
    }
}
