/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.security.hibernate;

import eu.fraho.spring.security.hibernate.spring.TestHibernateApiApplication;
import eu.fraho.spring.security.base.service.RefreshTokenStore;
import eu.fraho.spring.security.base.ut.service.AbstractTestJwtTokenServiceWithRefresh;
import lombok.Getter;
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
public class TestJwtServiceRefreshHibernate extends AbstractTestJwtTokenServiceWithRefresh {
    @Autowired
    private RefreshTokenStore refreshTokenStore;

    public TestJwtServiceRefreshHibernate() throws IOException {
    }

    @Override
    @NotNull
    protected RefreshTokenStore getRefreshStore() {
        return refreshTokenStore;
    }
}
