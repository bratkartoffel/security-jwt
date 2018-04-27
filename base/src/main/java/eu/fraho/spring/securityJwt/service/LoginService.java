/*
 * MIT Licence
 * Copyright (c) 2018 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.service;

import eu.fraho.spring.securityJwt.dto.AuthenticationRequest;
import eu.fraho.spring.securityJwt.dto.AuthenticationResponse;

public interface LoginService {
    AuthenticationResponse checkLogin(AuthenticationRequest authenticationRequest);
}
