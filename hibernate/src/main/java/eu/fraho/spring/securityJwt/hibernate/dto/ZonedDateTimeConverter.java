/*
 * MIT Licence
 * Copyright (c) 2020 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.hibernate.dto;

import javax.persistence.AttributeConverter;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

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
