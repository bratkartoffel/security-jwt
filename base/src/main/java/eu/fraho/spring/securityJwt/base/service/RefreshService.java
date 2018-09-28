/*
 * MIT Licence
 * Copyright (c) 2018 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.service;

import eu.fraho.spring.securityJwt.base.dto.AuthenticationResponse;
import org.springframework.security.core.AuthenticationException;

public interface RefreshService {
    AuthenticationResponse checkRefresh(String token) throws AuthenticationException;
}
