package eu.fraho.spring.securityJwt.util;

import com.nimbusds.jwt.JWTClaimsSet;
import eu.fraho.spring.securityJwt.dto.JwtUser;
import lombok.Getter;
import lombok.Setter;

public class MyJwtUser extends JwtUser {
    @Getter
    @Setter
    private String foobar;

    @Override
    public void applyClaims(JWTClaimsSet claims) {
        super.applyClaims(claims);
        setFoobar(String.valueOf(claims.getClaim("foobar")));
    }

    public JWTClaimsSet.Builder toClaims() {
        JWTClaimsSet.Builder builder = super.toClaims();
        builder.claim("foobar", foobar);
        return builder;
    }
}