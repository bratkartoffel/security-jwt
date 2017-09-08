package eu.fraho.spring.security.base.util;

import com.nimbusds.jwt.JWTClaimsSet;
import eu.fraho.spring.security.base.dto.JwtUser;
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