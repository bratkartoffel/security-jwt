/*
 * MIT Licence
 * Copyright (c) 2026 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.files.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.fraho.spring.securityJwt.base.exceptions.RefreshException;
import eu.fraho.spring.securityJwt.files.dto.DatabaseEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FilesTokenStore extends CommonFilesTokenStore {
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
        }
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
