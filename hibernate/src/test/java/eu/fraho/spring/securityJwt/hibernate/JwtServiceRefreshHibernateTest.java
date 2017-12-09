/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.hibernate;

import eu.fraho.spring.securityJwt.hibernate.spring.TestHibernateApiApplication;
import eu.fraho.spring.securityJwt.service.RefreshTokenStore;
import eu.fraho.spring.securityJwt.ut.service.AbstractJwtTokenServiceWithRefreshTest;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@Getter
@Slf4j
@SpringBootTest(properties = "spring.config.location=classpath:hibernate-test.yaml",
        classes = TestHibernateApiApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class JwtServiceRefreshHibernateTest extends AbstractJwtTokenServiceWithRefreshTest {
    @Setter(onMethod = @__({@Autowired, @NonNull}))
    private RefreshTokenStore refreshTokenStore;

    public JwtServiceRefreshHibernateTest() throws IOException {
    }

    @Override
    @NotNull
    protected RefreshTokenStore getRefreshStore() {
        return refreshTokenStore;
    }
}
