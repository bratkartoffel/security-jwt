/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.hibernate;

import eu.fraho.spring.securityJwt.base.it.AbstractAuthControllerWithRefreshTest;
import eu.fraho.spring.securityJwt.hibernate.spring.TestHibernateApiApplication;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.config.location=classpath:hibernate-test.yaml",
        classes = TestHibernateApiApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
//@Commit
@Transactional
public class AuthControllerHibernateTest extends AbstractAuthControllerWithRefreshTest {
}
