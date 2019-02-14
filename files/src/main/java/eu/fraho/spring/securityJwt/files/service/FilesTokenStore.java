/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.files.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.fraho.spring.securityJwt.base.config.RefreshProperties;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import eu.fraho.spring.securityJwt.base.dto.RefreshToken;
import eu.fraho.spring.securityJwt.base.dto.TimeWithPeriod;
import eu.fraho.spring.securityJwt.base.exceptions.RefreshException;
import eu.fraho.spring.securityJwt.base.service.RefreshTokenStore;
import eu.fraho.spring.securityJwt.files.config.FilesProperties;
import eu.fraho.spring.securityJwt.files.dto.DatabaseEntry;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@Slf4j
@NoArgsConstructor
public class FilesTokenStore implements RefreshTokenStore {
    private RefreshProperties refreshProperties;

    private UserDetailsService userDetailsService;

    private FilesProperties filesProperties;

    private ObjectMapper objectMapper;

    private ReentrantLock lock = new ReentrantLock(true);

    private List<DatabaseEntry> database = null;

    private void withLock(Consumer<List<DatabaseEntry>> consumer) {
        withLock((db) -> {
            consumer.accept(db);
            return null;
        });
    }

    private <T> T withLock(Function<List<DatabaseEntry>, T> consumer) {
        try {
            if (!lock.tryLock(5, TimeUnit.SECONDS)) {
                throw new RefreshException("Lock timed out");
            }
            if (filesProperties.isExternalLocks()) {
                try (FileOutputStream fos = new FileOutputStream(filesProperties.getDataDir().resolve("__lock").toFile());
                     FileLock ignored = fos.getChannel().lock()) {
                    return consumer.apply(loadDatabase());
                }
            } else {
                return consumer.apply(loadDatabase());
            }
        } catch (InterruptedException ie) {
            throw new RefreshException("Interruped", ie);
        } catch (IOException ioe) {
            throw new RefreshException("Storing token failed", ioe);
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }

    private List<DatabaseEntry> loadDatabase() {
        if (filesProperties.isExternalLocks() || database == null) {
            try {
                database = objectMapper.readValue(Files.readAllBytes(filesProperties.getDatabaseFile()), new TypeReference<List<DatabaseEntry>>() {
                });
            } catch (IOException ioe) {
                throw new RefreshException("Unable to load databaseFile, the file seems to be corrupted. Refreshtokens won't work until the file is fixed!", ioe);
            }
        }

        ZonedDateTime now = ZonedDateTime.now();
        database.removeIf(e -> e.getExpires().isBefore(now));
        return database;
    }

    private void saveDatabase(List<DatabaseEntry> database) {
        try {
            Files.write(filesProperties.getDatabaseFile(), objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(database));
        } catch (IOException ioe) {
            throw new RefreshException("Unable to save databaseFile", ioe);
        }
    }

    @Override
    public void saveToken(JwtUser user, String token) {
        withLock(db -> {
            TimeWithPeriod expiration = refreshProperties.getExpiration();
            db.add(
                    DatabaseEntry.builder()
                            .userId(user.getId())
                            .token(token)
                            .username(user.getUsername())
                            .expires(ZonedDateTime.now().plus(expiration.getQuantity(), expiration.getChronoUnit()))
                            .build());
            saveDatabase(db);
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends JwtUser> Optional<T> useToken(String token) {
        return withLock(db -> {
            return db.stream().filter(e -> Objects.equals(e.getToken(), token)).findAny().map(e -> {
                T user = (T) userDetailsService.loadUserByUsername(e.getUsername());
                if (db.remove(e)) {
                    saveDatabase(db);
                    return user;
                } else {
                    return null;
                }
            });
        });
    }


    @Override
    public List<RefreshToken> listTokens(JwtUser user) {
        return withLock(db -> {
            long now = ZonedDateTime.now().toEpochSecond();
            return Collections.unmodifiableList(db.stream()
                    .filter(e -> Objects.equals(e.getUserId(), user.getId()))
                    .map(e -> RefreshToken.builder().token(e.getToken()).expiresIn(e.getExpires().toEpochSecond() - now).build())
                    .collect(Collectors.toList()));
        });
    }


    @Override
    public Map<Long, List<RefreshToken>> listTokens() {
        return withLock(db -> {
            long now = ZonedDateTime.now().toEpochSecond();
            Map<Long, List<RefreshToken>> result = new HashMap<>();
            for (DatabaseEntry entry : db) {
                RefreshToken token = RefreshToken.builder().token(entry.getToken()).expiresIn(entry.getExpires().toEpochSecond() - now).build();
                result.computeIfAbsent(entry.getUserId(), s -> new ArrayList<>()).add(token);
            }
            return Collections.unmodifiableMap(result);
        });
    }

    @Override
    public boolean revokeToken(String token) {
        return withLock(db -> {
            Optional<DatabaseEntry> entry = db.stream().filter(e -> Objects.equals(e.getToken(), token)).findAny();
            if (entry.isPresent()) {
                db.remove(entry.get());
                saveDatabase(db);
            }
            return entry.isPresent();
        });
    }

    @Override
    public int revokeTokens(JwtUser user) {
        return withLock(db -> {
            List<DatabaseEntry> entries = db.stream().filter(e -> Objects.equals(e.getUserId(), user.getId())).collect(Collectors.toList());
            entries.forEach(db::remove);
            if (entries.size() > 0) saveDatabase(db);
            return entries.size();
        });
    }

    @Override
    public int revokeTokens() {
        return withLock(db -> {
            int count = db.size();
            db.clear();
            if (count > 0) saveDatabase(db);
            return count;
        });
    }

    @Override
    public void afterPropertiesSet() {
        log.info("Using files implementation to handle refresh tokens");
    }

    @Autowired
    public void setRefreshProperties(@NonNull RefreshProperties refreshProperties) {
        this.refreshProperties = refreshProperties;
    }

    @Autowired
    public void setUserDetailsService(@NonNull UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    public void setFilesProperties(@NonNull FilesProperties filesProperties) {
        this.filesProperties = filesProperties;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
