/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.dto;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "jwt_refresh")
@Getter
@Setter
@EqualsAndHashCode(of = {"userId", "username", "token"})
@ToString(of = {"id", "userId", "username", "token"})
@NoArgsConstructor
public class RefreshTokenEntity {
    @Id
    @GeneratedValue
    @Setter(AccessLevel.NONE)
    private Long id = 0L;

    @NotNull
    @Column(updatable = false)
    @Setter(AccessLevel.NONE)
    @Convert(converter = ZonedDateTimeConverter.class)
    private ZonedDateTime created = ZonedDateTime.now();

    @NotNull
    private Long userId;

    @NotNull
    private String username;

    @NotNull
    @Column(unique = true)
    private String token;

    public RefreshTokenEntity(Long userId, String username, String token) {
        this.userId = userId;
        this.username = username;
        this.token = token;
    }
}
