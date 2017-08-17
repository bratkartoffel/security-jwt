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
@Table(name = "jwt_refresh", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username", "deviceId"})
})
@Getter
@Setter
@EqualsAndHashCode(of = {"username", "deviceId", "token"})
@ToString(of = {"id", "username", "deviceId"})
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
    private String username;

    @NotNull
    private String deviceId;

    @NotNull
    private String token;

    public RefreshTokenEntity(String username, String deviceId, String token) {
        this.username = username;
        this.deviceId = deviceId;
        this.token = token;
    }
}
