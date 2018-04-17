package eu.fraho.spring.securityJwt.util;

import com.nimbusds.jwt.JWTClaimsSet;
import eu.fraho.spring.securityJwt.dto.JwtUser;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;

@Slf4j
public class MyJwtUser extends JwtUser {
    @Getter
    @Setter
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
}