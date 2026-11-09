package eu.fraho.spring.securityJwt.base.dto;

import java.time.LocalDateTime;

public record UsedTotpEntry(LocalDateTime lastUpdate, int code) {
}
