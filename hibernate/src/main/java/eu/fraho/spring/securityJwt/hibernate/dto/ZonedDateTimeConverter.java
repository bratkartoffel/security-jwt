/*
 * MIT Licence
 * Copyright (c) 2026 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.hibernate.dto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

@Converter
public class ZonedDateTimeConverter implements AttributeConverter<ZonedDateTime, Timestamp> {
    @Override
    public Timestamp convertToDatabaseColumn(ZonedDateTime zdt) {
        return Optional.ofNullable(zdt)
                .map(ZonedDateTime::toInstant)
                .map(Timestamp::from)
                .orElse(null);
    }

    @Override
    public ZonedDateTime convertToEntityAttribute(Timestamp ts) {
        return Optional.ofNullable(ts)
                .map(Timestamp::toInstant)
                .map(v -> ZonedDateTime.ofInstant(v, ZoneOffset.UTC))
                .orElse(null);
    }
}
