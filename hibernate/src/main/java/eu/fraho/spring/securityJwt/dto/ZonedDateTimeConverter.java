package eu.fraho.spring.securityJwt.dto;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
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

