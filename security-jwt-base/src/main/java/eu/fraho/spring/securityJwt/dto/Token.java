package eu.fraho.spring.securityJwt.dto;

import net.jcip.annotations.Immutable;

@Immutable
public abstract class Token {
    private final String token;
    private final int expiresIn;

    public Token(String token, int expiresIn) {
        this.token = token;
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public int getExpiresIn() {
        return expiresIn;
    }
}
