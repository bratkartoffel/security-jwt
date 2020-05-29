/*
 * MIT Licence
 * Copyright (c) 2020 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.hibernate;

import eu.fraho.spring.securityJwt.base.it.AbstractAuthControllerWithRefreshTest;
import eu.fraho.spring.securityJwt.hibernate.spring.TestHibernateApiApplication;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.config.location=classpath:hibernate-test.yaml", classes = TestHibernateApiApplication.class)
@ExtendWith(SpringExtension.class)
@Transactional
public class AuthControllerHibernateTest extends AbstractAuthControllerWithRefreshTest {
}
