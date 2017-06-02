/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.jcip.annotations.Immutable;

@Getter
@Immutable
@EqualsAndHashCode(callSuper = true)
public final class RefreshToken extends Token {
    private final String deviceId;

    @JsonCreator
    public RefreshToken(@JsonProperty("token") String token, @JsonProperty("expiresIn") int expiresIn, @JsonProperty("deviceId") String deviceId) {
        super(token, expiresIn);
        this.deviceId = deviceId;
    }
}
