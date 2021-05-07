/*
 * MIT Licence
 * Copyright (c) 2021 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.tests.it;

import eu.fraho.spring.securityJwt.base.dto.AccessToken;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import eu.fraho.spring.securityJwt.base.it.spring.TestApiApplication;
import eu.fraho.spring.securityJwt.base.service.JwtTokenService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Objects;
import java.util.Optional;

@SpringBootTest(classes = TestApiApplication.class)
@ExtendWith(SpringExtension.class)
public class JwtTokenServiceTest {
    private JwtTokenService jwtTokenService;

    private UserDetailsService userDetailsService;

    @Test
    public void testJwtUserNotSameInstance() throws Exception {
        JwtUser userFromDb = (JwtUser) userDetailsService.loadUserByUsername("foo");
        AccessToken token = jwtTokenService.generateToken(userFromDb);

        Optional<JwtUser> userFromService1 = jwtTokenService.parseUser(token.getToken());
        Optional<JwtUser> userFromService2 = jwtTokenService.parseUser(token.getToken());

        Assertions.assertTrue(userFromService1.isPresent(), "JwtUser1 should be parsed");
        Assertions.assertTrue(userFromService2.isPresent(), "JwtUser2 should be parsed");

        Assertions.assertNotSame(userFromDb, userFromService2.get(), "JwtUsers should be not the same instance");
        Assertions.assertNotSame(userFromService1.get(), userFromService2.get(), "JwtUsers should be not the same instance");
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

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = Objects.requireNonNull(userDetailsService);
    }
}
