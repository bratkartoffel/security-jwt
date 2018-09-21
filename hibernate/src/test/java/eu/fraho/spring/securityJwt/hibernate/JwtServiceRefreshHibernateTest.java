/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.hibernate;

import eu.fraho.spring.securityJwt.base.service.RefreshTokenStore;
import eu.fraho.spring.securityJwt.base.ut.service.AbstractJwtTokenServiceWithRefreshTest;
import eu.fraho.spring.securityJwt.hibernate.spring.TestHibernateApiApplication;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Objects;

@SpringBootTest(properties = "spring.config.location=classpath:hibernate-test.yaml",
        classes = TestHibernateApiApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class JwtServiceRefreshHibernateTest extends AbstractJwtTokenServiceWithRefreshTest {
    private RefreshTokenStore refreshTokenStore;

    public JwtServiceRefreshHibernateTest() throws IOException {
        super();
    }

    @Override
    protected RefreshTokenStore getRefreshStore() {
        return refreshTokenStore;
    }

    public RefreshTokenStore getRefreshTokenStore() {
        return this.refreshTokenStore;
    }

    @Autowired
    public void setRefreshTokenStore(RefreshTokenStore refreshTokenStore) {
        this.refreshTokenStore = Objects.requireNonNull(refreshTokenStore);
    }
}
