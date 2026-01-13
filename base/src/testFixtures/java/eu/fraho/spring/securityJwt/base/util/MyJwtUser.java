/*
 * MIT Licence
 * Copyright (c) 2026 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.util;

import com.nimbusds.jwt.JWTClaimsSet;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;

import java.text.ParseException;

public class MyJwtUser extends JwtUser {
    private String foobar;

    @Override
    public void applyClaims(JWTClaimsSet claims) throws ParseException {
        super.applyClaims(claims);
        setFoobar(claims.getStringClaim("foobar"));
    }

    public JWTClaimsSet.Builder toClaims() {
        JWTClaimsSet.Builder builder = super.toClaims();
        builder.claim("foobar", foobar);
        return builder;
    }

    public String getFoobar() {
        return this.foobar;
    }

    public void setFoobar(String foobar) {
        this.foobar = foobar;
    }
}