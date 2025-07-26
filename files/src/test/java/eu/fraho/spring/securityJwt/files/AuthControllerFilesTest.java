/*
 * MIT Licence
 * Copyright (c) 2025 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.files;

import eu.fraho.spring.securityJwt.base.it.AbstractAuthControllerWithRefreshTest;
import eu.fraho.spring.securityJwt.base.it.spring.TestApiApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest(properties = "spring.config.location=classpath:files-test.yaml", classes = TestApiApplication.class)
@ExtendWith(SpringExtension.class)
public class AuthControllerFilesTest extends AbstractAuthControllerWithRefreshTest {
    private static final Logger logger = LoggerFactory.getLogger(AuthControllerFilesTest.class);

    @AfterAll
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
