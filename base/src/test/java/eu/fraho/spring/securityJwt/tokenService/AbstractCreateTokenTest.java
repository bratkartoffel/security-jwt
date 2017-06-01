/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.tokenService;

import com.nimbusds.jose.JOSEException;
import eu.fraho.spring.securityJwt.AbstractTest;
import eu.fraho.spring.securityJwt.dto.AccessToken;
import eu.fraho.spring.securityJwt.service.JwtTokenServiceImpl;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
@Setter(AccessLevel.NONE)
@Slf4j
public abstract class AbstractCreateTokenTest extends AbstractTest {
    @Autowired
    protected JwtTokenServiceImpl jwtTokenService;

    @Test
    public void testCreateToken() throws JOSEException {
        AccessToken token = jwtTokenService.generateToken(getJwtUser());
        Assert.assertNotNull("No token generated", token.getToken());
        Assert.assertTrue("Token expired", jwtTokenService.validateToken(token));
        log.debug("Successfull token: {}", token.getToken());
    }

    @Test
    public void testValidateInvalidToken() throws JOSEException {
        Assert.assertFalse("Garbage should not validate", jwtTokenService.validateToken("this is sparta!!!"));
    }
}
