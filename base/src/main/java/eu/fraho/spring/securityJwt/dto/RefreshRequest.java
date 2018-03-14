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
import jdk.nashorn.internal.ir.annotations.Immutable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@JsonDeserialize(builder = RefreshRequest.RefreshRequestBuilder.class)
@Builder
@Immutable
@Value
public final class RefreshRequest {
    @JsonProperty(required = true)
    @NotNull
    @NonNull
    private final String refreshToken;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class RefreshRequestBuilder {
    }
}
