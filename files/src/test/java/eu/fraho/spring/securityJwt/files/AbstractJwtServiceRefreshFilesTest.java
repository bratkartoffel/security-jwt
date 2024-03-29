/*
 * MIT Licence
 * Copyright (c) 2022 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.files;

import eu.fraho.spring.securityJwt.base.service.RefreshTokenStore;
import eu.fraho.spring.securityJwt.base.ut.service.AbstractJwtTokenServiceWithRefreshTest;
import eu.fraho.spring.securityJwt.files.config.FilesProperties;
import eu.fraho.spring.securityJwt.files.service.FilesTokenStore;
import org.junit.jupiter.api.AfterAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AbstractJwtServiceRefreshFilesTest extends AbstractJwtTokenServiceWithRefreshTest {
    private static final Logger logger = LoggerFactory.getLogger(AbstractJwtServiceRefreshFilesTest.class);
    private final FilesTokenStore refreshTokenStore;

    public AbstractJwtServiceRefreshFilesTest() throws Exception {
        super();
        refreshTokenStore = new FilesTokenStore();
        refreshTokenStore.setRefreshProperties(getRefreshProperties());
        refreshTokenStore.setUserDetailsService(getUserdetailsService());
        refreshTokenStore.setFilesProperties(getFilesProperties());
        refreshTokenStore.setObjectMapper(getObjectMapper());
        refreshTokenStore.afterPropertiesSet();
    }

    @AfterAll
    public static void cleanup() {
        try {
            Files.deleteIfExists(Paths.get("test_datadir/db.json"));
        } catch (IOException e) {
            logger.error("Could not cleanup databaseFile", e);
        }
        try {
            Files.deleteIfExists(Paths.get("test_datadir/__lock"));
        } catch (IOException e) {
            logger.error("Could not cleanup databaseFile", e);
        }
        try {
            Files.deleteIfExists(Paths.get("test_datadir/"));
        } catch (IOException e) {
            logger.error("Could not cleanup databaseDir", e);
        }
    }

    private FilesProperties getFilesProperties() throws IOException {
        FilesProperties result = new FilesProperties();
        result.setDataDir(Paths.get("test_datadir/"));
        result.setExternalLocks(true);
        result.afterPropertiesSet();
        return result;
    }

    @Override
    protected RefreshTokenStore getRefreshStore() {
        return refreshTokenStore;
    }
}
