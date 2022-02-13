/*
 * MIT Licence
 * Copyright (c) 2022 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.service;

import eu.fraho.spring.securityJwt.base.dto.AuthenticationRequest;
import eu.fraho.spring.securityJwt.base.dto.AuthenticationResponse;

public interface LoginService {
    AuthenticationResponse checkLogin(AuthenticationRequest authenticationRequest);
}
