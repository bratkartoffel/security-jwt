/*
 * MIT Licence
 * Copyright (c) 2021 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.hibernate;

import eu.fraho.spring.securityJwt.base.it.AbstractAuthControllerWithRefreshTest;
import eu.fraho.spring.securityJwt.base.it.spring.TestApiApplication;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(properties = "spring.config.location=classpath:hibernate-test.yaml", classes = TestApiApplication.class)
@EntityScan(basePackages = {"eu.fraho.spring.securityJwt.hibernate"})
@ExtendWith(SpringExtension.class)
public class AuthControllerHibernateTest extends AbstractAuthControllerWithRefreshTest {
}
