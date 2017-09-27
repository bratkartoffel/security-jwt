/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.jcip.annotations.Immutable;

@Immutable
@Getter
@AllArgsConstructor
public final class AuthenticationResponse {
    private AccessToken accessToken;
    private RefreshToken refreshToken;
}
