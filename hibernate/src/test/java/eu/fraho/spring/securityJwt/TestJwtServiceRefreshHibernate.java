/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt;

import eu.fraho.spring.securityJwt.service.HibernateTokenStore;
import eu.fraho.spring.securityJwt.service.RefreshTokenStore;
import eu.fraho.spring.securityJwt.spring.TestHibernateApiApplication;
import eu.fraho.spring.securityJwt.tokenService.AbstractRefreshTokenTest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@Getter
@Setter(AccessLevel.NONE)
@Slf4j
@SpringBootTest(properties = "spring.config.location=classpath:hibernate-test.yaml",
        classes = TestHibernateApiApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TestJwtServiceRefreshHibernate extends AbstractRefreshTokenTest {
    @Autowired
    private RefreshTokenStore refreshTokenStore;

    @BeforeClass
    public static void beforeClass() throws IOException {
        AbstractTest.beforeHmacClass();
    }

    @Test
    @Override
    public void checkCorrectImplementationInUse() {
        final String name = refreshTokenStore.getClass().getName();
        final String realName = name.substring(0, name.indexOf("$$")); // workaround for cglib proxy
        Assert.assertEquals("Wrong implementation loaded", HibernateTokenStore.class.getName(), realName);
    }
}
