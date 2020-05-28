/*
 * MIT Licence
 * Copyright (c) 2020 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.redis;

import eu.fraho.spring.securityJwt.base.it.AbstractAuthControllerWithRefreshTest;
import eu.fraho.spring.securityJwt.base.it.spring.TestApiApplication;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest(properties = "spring.config.location=classpath:${REDIS_CONFIG:redis-test.yaml}",
        classes = TestApiApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class AuthControllerRedisTest extends AbstractAuthControllerWithRefreshTest {
}
