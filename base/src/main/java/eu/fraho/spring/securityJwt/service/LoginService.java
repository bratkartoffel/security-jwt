/*
 * MIT Licence
 * Copyright (c) 2018 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.service;

import eu.fraho.spring.securityJwt.dto.AuthenticationRequest;
import eu.fraho.spring.securityJwt.dto.AuthenticationResponse;
import eu.fraho.spring.securityJwt.dto.JwtUser;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LoginService {
    AuthenticationResponse checkLogin(AuthenticationRequest authenticationRequest);

    boolean isTotpOk(@Nullable Integer totp, @NotNull @NonNull JwtUser userDetails);
}
