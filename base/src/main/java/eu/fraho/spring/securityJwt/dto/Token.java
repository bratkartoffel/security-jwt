/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.jcip.annotations.Immutable;

@Getter
@Immutable
@EqualsAndHashCode(exclude = {"expiresIn"})
public abstract class Token {
    private final String token;
    private final int expiresIn;

    public Token(String token, int expiresIn) {
        this.token = token;
        this.expiresIn = expiresIn;
    }
}
