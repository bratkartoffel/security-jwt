/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import net.jcip.annotations.Immutable;

@Immutable
@Value
@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class JwtAuthenticationResponse {
    private AccessToken accessToken;
    private RefreshToken refreshToken;
}
