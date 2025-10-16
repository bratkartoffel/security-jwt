package eu.fraho.spring.securityJwt.files.service;

import eu.fraho.spring.securityJwt.base.config.RefreshProperties;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import eu.fraho.spring.securityJwt.base.dto.RefreshToken;
import eu.fraho.spring.securityJwt.base.dto.TimeWithPeriod;
import eu.fraho.spring.securityJwt.base.exceptions.RefreshException;
import eu.fraho.spring.securityJwt.base.service.RefreshTokenStore;
import eu.fraho.spring.securityJwt.files.config.FilesProperties;
import eu.fraho.spring.securityJwt.files.dto.DatabaseEntry;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

abstract class CommonFilesTokenStore implements RefreshTokenStore {
    private final ReentrantLock lock = new ReentrantLock(true);

    protected RefreshProperties refreshProperties;
    protected UserDetailsService userDetailsService;
    protected FilesProperties filesProperties;

    protected void withLock(Consumer<List<DatabaseEntry>> consumer) {
        withLock(db -> {
            consumer.accept(db);
            return null;
        });
    }

    protected <T> T withLock(Function<List<DatabaseEntry>, T> consumer) {
        try {
            if (!lock.tryLock(5, TimeUnit.SECONDS)) {
                throw new RefreshException("Lock timed out");
            }
            if (filesProperties.isExternalLocks()) {
                try (FileOutputStream fos = new FileOutputStream(filesProperties.getDataDir().resolve("__lock").toFile());
                     FileLock ignored = fos.getChannel().lock()) {
                    return consumer.apply(getDatabase());
                }
            } else {
                return consumer.apply(getDatabase());
            }
        } catch (InterruptedException ie) {
            throw new RefreshException("Interrupted", ie);
        } catch (IOException ioe) {
            throw new RefreshException("Storing token failed", ioe);
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }

    protected abstract List<DatabaseEntry> getDatabase();

    protected abstract void saveDatabase(List<DatabaseEntry> database);

    @Override
    public void saveToken(JwtUser user, String token) {
        withLock(db -> {
            TimeWithPeriod expiration = refreshProperties.getExpiration();
            db.add(DatabaseEntry.builder()
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
            return db.stream()
                    .filter(e -> Objects.equals(e.getUserId(), user.getId()))
                    .map(e -> RefreshToken.builder().token(e.getToken()).expiresIn(e.getExpires().toEpochSecond() - now).build())
                    .toList();
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
            List<DatabaseEntry> entries = db.stream().filter(e -> Objects.equals(e.getUserId(), user.getId())).toList();
            entries.forEach(db::remove);
            if (!entries.isEmpty()) saveDatabase(db);
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
}
