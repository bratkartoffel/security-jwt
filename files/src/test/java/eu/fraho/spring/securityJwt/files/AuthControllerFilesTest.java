/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.files;

import eu.fraho.spring.securityJwt.base.it.AbstractAuthControllerWithRefreshTest;
import eu.fraho.spring.securityJwt.base.it.spring.TestApiApplication;
import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest(properties = "spring.config.location=classpath:files-test.yaml",
        classes = TestApiApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class AuthControllerFilesTest extends AbstractAuthControllerWithRefreshTest {
    private static final Logger logger = LoggerFactory.getLogger(AuthControllerFilesTest.class);

    @AfterClass
    public static void cleanup() {
        try {
            Files.deleteIfExists(Paths.get("test_datadir/db.json"));
        } catch (IOException e) {
            logger.error("Could not cleanup databaseFile", e);
        }
        try {
            Files.deleteIfExists(Paths.get("test_datadir/"));
        } catch (IOException e) {
            logger.error("Could not cleanup databaseDir", e);
        }
    }
}
