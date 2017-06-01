/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt;

import eu.fraho.spring.securityJwt.spring.TestApiApplication;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Getter
@Setter(AccessLevel.NONE)
@Slf4j
@SpringBootTest(properties = "spring.config.location=classpath:internal-test-auth.yaml",
        classes = TestApiApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TestAuthControllerInternal extends TestAuthControllerRefresh {
}
