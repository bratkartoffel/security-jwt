/*
 * MIT Licence
 * Copyright (c) 2025 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.files.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@ConfigurationProperties(prefix = "fraho.jwt.refresh.files")
@Component
@Getter
@Setter
@Slf4j
public class FilesProperties implements InitializingBean {
    /**
     * The directory where the {@link #databaseFile} and lockfile (if {@link #externalLocks} is used) are stored.<br>
     * The directory will be created if it doesn't exist.
     */
    private Path dataDir = Paths.get("data/");

    /**
     * Use a filesystem-level lockfile? Due to performance reasons this should only be used when other applications access the database.
     */
    private boolean externalLocks = false;

    /**
     * Filename of the database which stores the tokens, relative to {@link #dataDir}.<br>
     * An empty database will be initialized if it doesn't exist.
     */
    private Path databaseFile = Paths.get("db.json");

    @Override
    public void afterPropertiesSet() throws IOException {
        if (!Files.exists(dataDir)) {
            Files.createDirectories(dataDir);
        }
        databaseFile = dataDir.resolve(databaseFile);
        if (!Files.exists(databaseFile)) {
            Files.write(databaseFile, "[]".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);
        }
    }
}
