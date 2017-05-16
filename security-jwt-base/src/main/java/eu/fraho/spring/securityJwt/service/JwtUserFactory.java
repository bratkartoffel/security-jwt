package eu.fraho.spring.securityJwt.service;

import eu.fraho.spring.securityJwt.dto.JwtUser;

public interface JwtUserFactory {
    JwtUser convert(Object obj);
}
