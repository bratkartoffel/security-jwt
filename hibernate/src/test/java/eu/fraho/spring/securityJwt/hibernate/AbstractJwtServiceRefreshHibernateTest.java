/*
 * MIT Licence
 * Copyright (c) 2020 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.hibernate;

import eu.fraho.spring.securityJwt.base.service.RefreshTokenStore;
import eu.fraho.spring.securityJwt.base.ut.service.AbstractJwtTokenServiceWithRefreshTest;
import eu.fraho.spring.securityJwt.hibernate.spring.TestHibernateApiApplication;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Objects;

@SpringBootTest(properties = "spring.config.location=classpath:hibernate-test.yaml",
        classes = TestHibernateApiApplication.class)
@ExtendWith(SpringExtension.class)
public class AbstractJwtServiceRefreshHibernateTest extends AbstractJwtTokenServiceWithRefreshTest {
    private RefreshTokenStore refreshTokenStore;

    public AbstractJwtServiceRefreshHibernateTest() throws IOException {
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
