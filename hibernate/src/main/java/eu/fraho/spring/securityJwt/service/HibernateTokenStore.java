/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.service;

import eu.fraho.spring.securityJwt.dto.RefreshToken;
import eu.fraho.spring.securityJwt.dto.RefreshTokenEntity;
import eu.fraho.spring.securityJwt.dto.TimeWithPeriod;
import eu.fraho.spring.securityJwt.exceptions.JwtRefreshException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
public class HibernateTokenStore implements RefreshTokenStore {
    @Value("${fraho.jwt.refresh.expiration:" + JwtTokenServiceImpl.DEFAULT_REFRESH_EXPIRATION + "}")
    private TimeWithPeriod refreshExpiration = new TimeWithPeriod(JwtTokenServiceImpl.DEFAULT_REFRESH_EXPIRATION);

    @PersistenceContext
    private EntityManager em = null;

    @Override
    @Transactional
    public void saveToken(String username, String deviceId, String token) {
        revokeToken(username, deviceId);
        RefreshTokenEntity entity = new RefreshTokenEntity(username, deviceId, token);
        try {
            em.persist(entity);
        } catch (Exception ex) {
            throw new JwtRefreshException("Could not persist refresh token", ex);
        }
    }

    @Override
    @Transactional
    public boolean useToken(String username, String deviceId, String token) {
        final Query query = em.createQuery("DELETE RefreshTokenEntity WHERE " +
                "username = :username AND deviceId = :deviceId AND token = :token AND created >= :expiration");
        query.setParameter("username", username);
        query.setParameter("deviceId", deviceId);
        query.setParameter("token", token);
        setExpiration(query);

        try {
            return query.executeUpdate() != 0;
        } catch (Exception ex) {
            throw new JwtRefreshException("Could not use refresh token", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefreshToken> listTokens(String username) {
        final TypedQuery<RefreshTokenEntity> query = em.createQuery("SELECT o FROM RefreshTokenEntity o WHERE " +
                "username = :username AND created >= :expiration", RefreshTokenEntity.class);
        query.setParameter("username", username);
        setExpiration(query);

        try {
            return query.getResultList().stream().map(e -> new RefreshToken(e.getToken(), calculateExpiration(e.getCreated()), e.getDeviceId())).collect(Collectors.toList());
        } catch (Exception ex) {
            throw new JwtRefreshException("Could not list refresh token", ex);
        }
    }

    private int calculateExpiration(ZonedDateTime created) {
        return (int) ChronoUnit.SECONDS.between(created, ZonedDateTime.now());
    }

    private void setExpiration(Query query) {
        final ZonedDateTime expiration = ZonedDateTime.now().minusSeconds(refreshExpiration.toSeconds());
        query.setParameter("expiration", expiration);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<RefreshToken>> listTokens() {
        final TypedQuery<RefreshTokenEntity> query = em.createQuery("SELECT o FROM RefreshTokenEntity o WHERE " +
                "created >= :expiration", RefreshTokenEntity.class);
        setExpiration(query);
        try {
            final List<RefreshTokenEntity> tokens = query.getResultList();
            final Map<String, List<RefreshToken>> result = new HashMap<>();

            tokens.forEach(e -> result.computeIfAbsent(e.getUsername(), s -> new ArrayList<>())
                    .add(new RefreshToken(e.getToken(), calculateExpiration(e.getCreated()), e.getDeviceId())));
            result.replaceAll((s, t) -> Collections.unmodifiableList(t));
            return Collections.unmodifiableMap(result);
        } catch (Exception ex) {
            throw new JwtRefreshException("Could not list refresh token", ex);
        }
    }

    @Override
    @Transactional
    public boolean revokeToken(String username, RefreshToken token) {
        return revokeToken(username, token.getDeviceId());
    }

    @Override
    @Transactional
    public boolean revokeToken(String username, String deviceId) {
        final Query query = em.createQuery("DELETE RefreshTokenEntity WHERE " +
                "username = :username AND deviceId = :deviceId");
        query.setParameter("username", username);
        query.setParameter("deviceId", deviceId);

        try {
            return query.executeUpdate() != 0;
        } catch (Exception ex) {
            throw new JwtRefreshException("Could not revoke refresh token", ex);
        }
    }

    @Override
    @Transactional
    public int revokeTokens(String username) {
        final Query query = em.createQuery("DELETE RefreshTokenEntity WHERE " +
                "username = :username");
        query.setParameter("username", username);

        try {
            return query.executeUpdate();
        } catch (Exception ex) {
            throw new JwtRefreshException("Could not revoke refresh token", ex);
        }
    }

    @Override
    @Transactional
    public int revokeTokens() {
        final Query query = em.createQuery("DELETE RefreshTokenEntity");

        try {
            return query.executeUpdate();
        } catch (Exception ex) {
            throw new JwtRefreshException("Could not revoke refresh token", ex);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // nothing to do here
    }

    @Override
    public TimeWithPeriod getRefreshExpiration() {
        return refreshExpiration;
    }
}
