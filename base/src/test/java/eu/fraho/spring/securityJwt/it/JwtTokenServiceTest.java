/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.it;

import eu.fraho.spring.securityJwt.dto.AccessToken;
import eu.fraho.spring.securityJwt.dto.JwtUser;
import eu.fraho.spring.securityJwt.it.spring.TestApiApplication;
import eu.fraho.spring.securityJwt.service.JwtTokenService;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;

@Getter
@Slf4j
@SpringBootTest(classes = TestApiApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class JwtTokenServiceTest {
    @Setter(onMethod = @__({@Autowired, @NonNull}))
    private JwtTokenService jwtTokenService;

    @Setter(onMethod = @__({@Autowired, @NonNull}))
    private UserDetailsService userDetailsService;

    @Test
    public void testJwtUserNotSameInstance() throws Exception {
        JwtUser userFromDb = (JwtUser) userDetailsService.loadUserByUsername("foo");
        AccessToken token = jwtTokenService.generateToken(userFromDb);

        Optional<JwtUser> userFromService1 = jwtTokenService.parseUser(token.getToken());
        Optional<JwtUser> userFromService2 = jwtTokenService.parseUser(token.getToken());

        Assert.assertTrue("JwtUser1 should be parsed", userFromService1.isPresent());
        Assert.assertTrue("JwtUser2 should be parsed", userFromService2.isPresent());

        Assert.assertFalse("JwtUsers should be not the same instance", userFromDb == userFromService2.get());
        Assert.assertFalse("JwtUsers should be not the same instance", userFromService1.get() == userFromService2.get());
    }
}
