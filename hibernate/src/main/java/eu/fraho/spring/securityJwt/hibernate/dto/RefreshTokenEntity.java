/*
 * MIT Licence
 * Copyright (c) 2020 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.hibernate.dto;

import lombok.*;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "jwt_refresh")
@Getter
@Setter
@EqualsAndHashCode(of = {"userId", "username", "token"})
@ToString(of = {"id", "userId", "username", "token"})
@NoArgsConstructor
public class RefreshTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id = 0L;

    @Column(updatable = false)
    @Setter(AccessLevel.NONE)
    @Convert(converter = ZonedDateTimeConverter.class)
    private ZonedDateTime created = ZonedDateTime.now();

    private Long userId;

    private String username;

    @Column(unique = true)
    private String token;

    @Builder
    private RefreshTokenEntity(Long userId, String username, String token) {
        this.userId = userId;
        this.username = username;
        this.token = token;
    }
}
