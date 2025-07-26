/*
 * MIT Licence
 * Copyright (c) 2025 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.memcache;

import eu.fraho.spring.securityJwt.base.it.AbstractAuthControllerWithRefreshTest;
import eu.fraho.spring.securityJwt.base.it.spring.TestApiApplication;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(properties = "spring.config.location=classpath:memcache-test.yaml", classes = TestApiApplication.class)
@ExtendWith(SpringExtension.class)
public class AuthControllerMemcacheTest extends AbstractAuthControllerWithRefreshTest {
}
