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

import java.util.Optional;

@JsonDeserialize(builder = AuthenticationRequest.AuthenticationRequestBuilder.class)
@Getter
@Builder
@Immutable
public final class AuthenticationRequest {
    @JsonProperty(required = true)

    @NonNull
    private final String username;

    @JsonProperty(required = true)

    @NonNull
    private final String password;


    private final Integer totp;

    public Optional<Integer> getTotp() {
        return Optional.ofNullable(totp);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class AuthenticationRequestBuilder {
    }
}
