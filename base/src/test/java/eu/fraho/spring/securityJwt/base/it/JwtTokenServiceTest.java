/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.it;

import eu.fraho.spring.securityJwt.base.dto.AccessToken;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import eu.fraho.spring.securityJwt.base.it.spring.TestApiApplication;
import eu.fraho.spring.securityJwt.base.service.JwtTokenService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Objects;
import java.util.Optional;

@SpringBootTest(classes = TestApiApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class JwtTokenServiceTest {
    private JwtTokenService jwtTokenService;

    private UserDetailsService userDetailsService;

    @Test
    public void testJwtUserNotSameInstance() throws Exception {
        JwtUser userFromDb = (JwtUser) userDetailsService.loadUserByUsername("foo");
        AccessToken token = jwtTokenService.generateToken(userFromDb);

        Optional<JwtUser> userFromService1 = jwtTokenService.parseUser(token.getToken());
        Optional<JwtUser> userFromService2 = jwtTokenService.parseUser(token.getToken());

        Assert.assertTrue("JwtUser1 should be parsed", userFromService1.isPresent());
        Assert.assertTrue("JwtUser2 should be parsed", userFromService2.isPresent());

        Assert.assertNotSame("JwtUsers should be not the same instance", userFromDb, userFromService2.get());
        Assert.assertNotSame("JwtUsers should be not the same instance", userFromService1.get(), userFromService2.get());
    }

    public JwtTokenService getJwtTokenService() {
        return this.jwtTokenService;
    }

    @Autowired
    public void setJwtTokenService(JwtTokenService jwtTokenService) {
        this.jwtTokenService = Objects.requireNonNull(jwtTokenService);
    }

    public UserDetailsService getUserDetailsService() {
        return this.userDetailsService;
    }

    @Autowired
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = Objects.requireNonNull(userDetailsService);
    }
}
