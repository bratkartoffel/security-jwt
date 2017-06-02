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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

@Getter
@Slf4j
public abstract class AbstractCreateTokenTest extends AbstractTest {
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
