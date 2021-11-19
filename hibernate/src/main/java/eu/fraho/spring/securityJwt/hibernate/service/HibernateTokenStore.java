/*
 * MIT Licence
 * Copyright (c) 2021 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.hibernate.service;

import eu.fraho.spring.securityJwt.base.config.RefreshProperties;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import eu.fraho.spring.securityJwt.base.dto.RefreshToken;
import eu.fraho.spring.securityJwt.base.service.RefreshTokenStore;
import eu.fraho.spring.securityJwt.hibernate.dto.RefreshTokenEntity;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings({"SpringJavaAutowiredMembersInspection", "JpaQlInspection"})
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class HibernateTokenStore implements RefreshTokenStore {
    private RefreshProperties refreshProperties;

    private UserDetailsService userDetailsService;

    private EntityManager entityManager;

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public <T extends JwtUser> Optional<T> useToken(String token) {
        ZonedDateTime expiration = ZonedDateTime.now().minusSeconds(refreshProperties.getExpiration().toSeconds());
        // first load the token from the database
        TypedQuery<RefreshTokenEntity> queryLoad = entityManager.createQuery("SELECT o FROM RefreshTokenEntity o WHERE o.token = :token AND o.created >= :expiration", RefreshTokenEntity.class);
        queryLoad.setParameter("token", token);
        queryLoad.setParameter("expiration", expiration);
        List<RefreshTokenEntity> loadResultList = queryLoad.getResultList();
        if (loadResultList.size() != 1) {
            return Optional.empty();
        }

        RefreshTokenEntity refreshToken = loadResultList.get(0);
        Optional<T> resultUser = Optional.ofNullable((T) userDetailsService.loadUserByUsername(refreshToken.getUsername()));

        if (resultUser.isPresent()) {
            Query query = entityManager.createQuery("DELETE FROM RefreshTokenEntity o WHERE o.id = :id");
            query.setParameter("id", refreshToken.getId());

            if (query.executeUpdate() == 0) {
                resultUser = Optional.empty();
            }
        }

        return resultUser;
    }


    @Override
    @Transactional(readOnly = true)
    public List<RefreshToken> listTokens(JwtUser user) {
        ZonedDateTime expiration = ZonedDateTime.now().minusSeconds(refreshProperties.getExpiration().toSeconds());
        TypedQuery<RefreshTokenEntity> query = entityManager.createQuery("SELECT o FROM RefreshTokenEntity o WHERE o.userId = :userId AND o.created >= :expiration", RefreshTokenEntity.class);
        query.setParameter("userId", user.getId());
        query.setParameter("expiration", expiration);

        List<RefreshTokenEntity> result = query.getResultList();
        return result.stream()
                .map(e -> RefreshToken.builder()
                        .token(e.getToken())
                        .expiresIn(calculateExpiration(e.getCreated()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void saveToken(JwtUser user, String token) {
        entityManager.persist(RefreshTokenEntity.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .token(token)
                .build());
    }


    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<RefreshToken>> listTokens() {
        TypedQuery<RefreshTokenEntity> query = entityManager.createQuery("SELECT o FROM RefreshTokenEntity o WHERE o.created >= :expiration", RefreshTokenEntity.class);
        ZonedDateTime expiration = ZonedDateTime.now().minusSeconds(refreshProperties.getExpiration().toSeconds());
        query.setParameter("expiration", expiration);

        List<RefreshTokenEntity> tokens = query.getResultList();
        Map<Long, List<RefreshToken>> result = new HashMap<>();

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
    public boolean revokeToken(String token) {
        Query query = entityManager.createQuery("DELETE FROM RefreshTokenEntity o WHERE o.token = :token ");
        query.setParameter("token", token);
        return query.executeUpdate() != 0;
    }

    @Override
    @Transactional
    public int revokeTokens(JwtUser user) {
        Query query = entityManager.createQuery("DELETE FROM RefreshTokenEntity o WHERE o.userId = :userId");
        query.setParameter("userId", user.getId());
        return query.executeUpdate();
    }

    @Override
    @Transactional
    public int revokeTokens() {
        Query query = entityManager.createQuery("DELETE FROM RefreshTokenEntity o");
        return query.executeUpdate();
    }

    @Autowired
    public void setRefreshProperties(@NonNull RefreshProperties refreshProperties) {
        this.refreshProperties = refreshProperties;
    }

    @Autowired
    public void setUserDetailsService(@NonNull UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @PersistenceContext
    public void setEntityManager(@NonNull EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void afterPropertiesSet() {
        log.info("Using hibernate implementation to handle refresh tokens");
    }

    protected int calculateExpiration(ZonedDateTime created) {
        return (int) ChronoUnit.SECONDS.between(created, ZonedDateTime.now());
    }
}
