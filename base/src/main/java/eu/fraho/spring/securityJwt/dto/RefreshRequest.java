/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import net.jcip.annotations.Immutable;

@Immutable
@JsonDeserialize(builder = RefreshRequest.RefreshRequestBuilder.class)
@Builder
@Value
public final class RefreshRequest {
    private String refreshToken;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class RefreshRequestBuilder {
    }
}
