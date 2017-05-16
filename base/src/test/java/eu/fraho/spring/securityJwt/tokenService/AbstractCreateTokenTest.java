package eu.fraho.spring.securityJwt.tokenService;

import com.nimbusds.jose.JOSEException;
import eu.fraho.spring.securityJwt.dto.AccessToken;
import eu.fraho.spring.securityJwt.service.JwtTokenServiceImplAccessor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

@Getter
@Setter(AccessLevel.NONE)
@Slf4j
public abstract class AbstractCreateTokenTest extends JwtTokenServiceImplAccessor {
    @Test
    public void testCreateToken() throws JOSEException, InterruptedException {
        AccessToken token = jwtTokenService.generateToken(getJwtUser());
        Assert.assertNotNull("No token generated", token.getToken());
        Assert.assertTrue("Token expired", jwtTokenService.validateToken(token));
    }
}
