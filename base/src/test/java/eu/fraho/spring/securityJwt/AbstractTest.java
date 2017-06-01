/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt;

import eu.fraho.spring.securityJwt.dto.JwtUser;
import eu.fraho.spring.securityJwt.service.JwtTokenServiceImpl;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Collections;

@Slf4j
public abstract class AbstractTest {
    public static final String OUT_KEY = "build/hmac-hs256.key";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Autowired
    @Getter
    protected JwtTokenServiceImpl jwtTokenService = null;

    public static void beforeHmacClass() throws IOException {
        checkAndCreateOutDirs(OUT_KEY);

        SecureRandom random = new SecureRandom();
        byte[] data = new byte[32];
        random.nextBytes(data);
        Files.write(Paths.get(OUT_KEY), data);
    }

    public static void checkAndCreateOutDirs(String path) throws NoSuchFileException {
        final File parent = Paths.get(path).getParent().toFile();
        if (!parent.exists()) {
            log.info("Creating output directory: {}", parent.getAbsolutePath());
            if (!parent.mkdirs()) {
                log.error("Could not create directory");
                throw new NoSuchFileException("Could not create directory: " + parent.getAbsolutePath());
            }
        }
    }

    protected JwtUser getJwtUser() {
        JwtUser user = new JwtUser();
        user.setId(42L);
        user.setUsername("jsmith");
        user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        return user;
    }


    protected void withTempField(String fieldname, Object value, Runnable callback) {
        try {
            final Field field = JwtTokenServiceImpl.class.getDeclaredField(fieldname);
            field.setAccessible(true);
            Object oldValue = field.get(jwtTokenService);
            try {
                field.set(jwtTokenService, value);
                callback.run();
            } finally {
                field.set(jwtTokenService, oldValue);
                callback.run();
            }
        } catch (RuntimeException rex) {
            throw rex;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
