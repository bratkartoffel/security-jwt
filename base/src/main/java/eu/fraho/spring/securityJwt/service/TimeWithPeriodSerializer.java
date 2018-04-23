/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import eu.fraho.spring.securityJwt.dto.TimeWithPeriod;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public final class TimeWithPeriodSerializer extends StdSerializer<TimeWithPeriod> {
    @SuppressWarnings("unused")
    public TimeWithPeriodSerializer() {
        this(null);
    }

    public TimeWithPeriodSerializer(Class<TimeWithPeriod> t) {
        super(t);
    }

    @Override
    public void serialize(TimeWithPeriod timeWithPeriod, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(timeWithPeriod.toString());
    }
}