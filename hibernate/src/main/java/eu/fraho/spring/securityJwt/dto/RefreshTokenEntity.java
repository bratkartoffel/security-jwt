/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

@Entity
@Table(name = "jwt_refresh", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username", "deviceId"})
})
@Data
@NoArgsConstructor
public class RefreshTokenEntity {
    @Id
    @GeneratedValue
    @Setter(AccessLevel.NONE)
    private Long id = 0L;

    @NotNull
    @Column(updatable = false)
    @Setter(AccessLevel.NONE)
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
