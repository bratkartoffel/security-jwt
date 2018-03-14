/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.hibernate.service;

import eu.fraho.spring.securityJwt.config.RefreshProperties;
import eu.fraho.spring.securityJwt.dto.JwtUser;
import eu.fraho.spring.securityJwt.dto.RefreshToken;
import eu.fraho.spring.securityJwt.hibernate.dto.RefreshTokenEntity;
import eu.fraho.spring.securityJwt.service.RefreshTokenStore;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
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
@NoArgsConstructor
@AllArgsConstructor
public class HibernateTokenStore implements RefreshTokenStore {
    @Setter(onMethod = @__({@Autowired, @NonNull}))
    private RefreshProperties refreshProperties;

    @Setter(onMethod = @__({@Autowired, @NonNull}))
    private UserDetailsService userDetailsService;

    @Setter(onMethod = @__({@PersistenceContext, @NonNull}))
    private EntityManager entityManager;

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public <T extends JwtUser> Optional<T> useToken(@NotNull String token) {
        // first load the token from the database
        final TypedQuery<RefreshTokenEntity> queryLoad = entityManager.createQuery("SELECT o FROM RefreshTokenEntity o WHERE " +
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
            final Query query = entityManager.createQuery("DELETE FROM RefreshTokenEntity o WHERE o.id = :id");
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
        final TypedQuery<RefreshTokenEntity> query = entityManager.createQuery("SELECT o FROM RefreshTokenEntity o WHERE " +
                "o.userId = :userId AND o.created >= :expiration", RefreshTokenEntity.class);
        query.setParameter("userId", user.getId());
        setQueryExpiration(query);

        List<RefreshTokenEntity> result = query.getResultList();
        return result.stream()
                .map(e -> RefreshToken.builder()
                        .token(e.getToken())
                        .expiresIn(calculateExpiration(e.getCreated()))
                        .build())
                .collect(Collectors.toList());
    }

    private int calculateExpiration(@NotNull ZonedDateTime created) {
        return (int) ChronoUnit.SECONDS.between(created, ZonedDateTime.now());
    }

    private void setQueryExpiration(@NotNull Query query) {
        final ZonedDateTime expiration = ZonedDateTime.now().minusSeconds(refreshProperties.getExpiration().toSeconds());
        query.setParameter("expiration", expiration);
    }

    @Override
    @Transactional
    public void saveToken(@NotNull JwtUser user, @NotNull String token) {
        RefreshTokenEntity entity = new RefreshTokenEntity(user.getId(), user.getUsername(), token);
        entityManager.persist(entity);
    }

    @NotNull
    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<RefreshToken>> listTokens() {
        final TypedQuery<RefreshTokenEntity> query = entityManager.createQuery("SELECT o FROM RefreshTokenEntity o WHERE " +
                "o.created >= :expiration", RefreshTokenEntity.class);
        setQueryExpiration(query);

        final List<RefreshTokenEntity> tokens = query.getResultList();
        final Map<Long, List<RefreshToken>> result = new HashMap<>();

        tokens.forEach(e ->
                result.computeIfAbsent(e.getUserId(), s -> new ArrayList<>())
                        .add(RefreshToken.builder()
                                .token(e.getToken())
                                .expiresIn(calculateExpiration(e.getCreated()))
                                .build())
        );
        result.replaceAll((s, t) -> Collections.unmodifiableList(t));
        return Collections.unmodifiableMap(result);
    }

    @Override
    @Transactional
    public boolean revokeToken(@NotNull String token) {
        final Query query = entityManager.createQuery("DELETE FROM RefreshTokenEntity o WHERE " +
                "o.token = :token ");
        query.setParameter("token", token);

        return query.executeUpdate() != 0;
    }

    @Override
    @Transactional
    public int revokeTokens(@NotNull JwtUser user) {
        final Query query = entityManager.createQuery("DELETE FROM RefreshTokenEntity o WHERE " +
                "o.userId = :userId");
        query.setParameter("userId", user.getId());

        return query.executeUpdate();
    }

    @Override
    @Transactional
    public int revokeTokens() {
        final Query query = entityManager.createQuery("DELETE FROM RefreshTokenEntity o");
        return query.executeUpdate();
    }

    @Override
    public void afterPropertiesSet() {
        log.info("Using hibernate implementation to handle refresh tokens");
    }
}
