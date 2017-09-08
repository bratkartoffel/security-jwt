/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.security.hibernate;

import eu.fraho.spring.security.base.config.JwtRefreshConfiguration;
import eu.fraho.spring.security.base.dto.JwtUser;
import eu.fraho.spring.security.base.dto.RefreshToken;
import eu.fraho.spring.security.hibernate.dto.RefreshTokenEntity;
import eu.fraho.spring.security.base.service.RefreshTokenStore;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HibernateTokenStore implements RefreshTokenStore {
    @NonNull
    private final JwtRefreshConfiguration refreshConfig;

    @NonNull
    private UserDetailsService userDetailsService;

    @PersistenceContext
    private EntityManager em = null;

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public <T extends JwtUser> Optional<T> useToken(@NotNull String token) {
        // first load the token from the database
        final TypedQuery<RefreshTokenEntity> queryLoad = em.createQuery("SELECT o FROM RefreshTokenEntity o WHERE " +
                "o.token = :token AND o.created >= :expiration", RefreshTokenEntity.class);
        queryLoad.setParameter("token", token);
        setQueryExpiration(queryLoad);
        List<RefreshTokenEntity> loadResultList = queryLoad.getResultList();
        if (loadResultList.size() != 1) {
            return Optional.empty();
        }

        RefreshTokenEntity refreshToken = loadResultList.get(0);
        Optional<T> resultUser = Optional.ofNullable((T) userDetailsService.loadUserByUsername(refreshToken.getUsername()));

        if (resultUser.isPresent()) {
            final Query query = em.createQuery("DELETE FROM RefreshTokenEntity o WHERE o.id = :id");
            query.setParameter("id", refreshToken.getId());

            if (query.executeUpdate() == 0) {
                resultUser = Optional.empty();
            }
        }

        return resultUser;
    }

    @NotNull
    @Override
    @Transactional(readOnly = true)
    public List<RefreshToken> listTokens(@NotNull JwtUser user) {
        final TypedQuery<RefreshTokenEntity> query = em.createQuery("SELECT o FROM RefreshTokenEntity o WHERE " +
                "o.userId = :userId AND o.created >= :expiration", RefreshTokenEntity.class);
        query.setParameter("userId", user.getId());
        setQueryExpiration(query);

        List<RefreshTokenEntity> result = query.getResultList();
        return result.stream()
                .map(e -> new RefreshToken(e.getToken(), calculateExpiration(e.getCreated())))
                .collect(Collectors.toList());
    }

    private int calculateExpiration(@NotNull ZonedDateTime created) {
        return (int) ChronoUnit.SECONDS.between(created, ZonedDateTime.now());
    }

    private void setQueryExpiration(@NotNull Query query) {
        final ZonedDateTime expiration = ZonedDateTime.now().minusSeconds(refreshConfig.getExpiration().toSeconds());
        query.setParameter("expiration", expiration);
    }

    @Override
    @Transactional
    public void saveToken(@NotNull JwtUser user, @NotNull String token) {
        RefreshTokenEntity entity = new RefreshTokenEntity(user.getId(), user.getUsername(), token);
        em.persist(entity);
    }

    @NotNull
    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<RefreshToken>> listTokens() {
        final TypedQuery<RefreshTokenEntity> query = em.createQuery("SELECT o FROM RefreshTokenEntity o WHERE " +
                "o.created >= :expiration", RefreshTokenEntity.class);
        setQueryExpiration(query);

        final List<RefreshTokenEntity> tokens = query.getResultList();
        final Map<Long, List<RefreshToken>> result = new HashMap<>();

        tokens.forEach(e ->
                result.computeIfAbsent(e.getUserId(), s -> new ArrayList<>())
                        .add(new RefreshToken(e.getToken(), calculateExpiration(e.getCreated())))
        );
        result.replaceAll((s, t) -> Collections.unmodifiableList(t));
        return Collections.unmodifiableMap(result);
    }

    @Override
    @Transactional
    public boolean revokeToken(@NotNull String token) {
        final Query query = em.createQuery("DELETE FROM RefreshTokenEntity o WHERE " +
                "o.token = :token ");
        query.setParameter("token", token);

        return query.executeUpdate() != 0;
    }

    @Override
    @Transactional
    public int revokeTokens(@NotNull JwtUser user) {
        final Query query = em.createQuery("DELETE FROM RefreshTokenEntity o WHERE " +
                "o.userId = :userId");
        query.setParameter("userId", user.getId());

        return query.executeUpdate();
    }

    @Override
    @Transactional
    public int revokeTokens() {
        final Query query = em.createQuery("DELETE FROM RefreshTokenEntity o");
        return query.executeUpdate();
    }

    @Override
    public void afterPropertiesSet() {
        log.info("Using hibernate implementation to handle refresh tokens");
    }
}
