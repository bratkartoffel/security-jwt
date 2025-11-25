/*
 * MIT Licence
 * Copyright (c) 2025 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.files.service;

import eu.fraho.spring.securityJwt.base.exceptions.RefreshException;
import eu.fraho.spring.securityJwt.files.dto.DatabaseEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FilesTokenStoreJackson3 extends CommonFilesTokenStore {
    protected final List<DatabaseEntry> database = new ArrayList<>();
    private ObjectMapper objectMapper;

    protected List<DatabaseEntry> getDatabase() {
        ZonedDateTime now = ZonedDateTime.now();
        database.removeIf(e -> e.getExpires().isBefore(now));
        return database;
    }

    protected void saveDatabase(List<DatabaseEntry> database) {
        try {
            Files.write(filesProperties.getDatabaseFile(), objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(database));
        } catch (IOException ioe) {
            throw new RefreshException("Unable to save databaseFile", ioe);
        }
    }

    @Override
    public void afterPropertiesSet() throws IOException {
        log.info("Using files implementation to handle refresh tokens");
        if (Files.exists(filesProperties.getDatabaseFile())) {
            log.debug("Loading existing store");
            database.addAll(objectMapper.readValue(Files.readAllBytes(filesProperties.getDatabaseFile()), new TypeReference<>() {
            }));
            ZonedDateTime now = ZonedDateTime.now();
            database.removeIf(e -> e.getExpires().isBefore(now));
        }
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
