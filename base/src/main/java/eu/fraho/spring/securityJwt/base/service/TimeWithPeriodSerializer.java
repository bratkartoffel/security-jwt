/*
 * MIT Licence
 * Copyright (c) 2022 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import eu.fraho.spring.securityJwt.base.dto.TimeWithPeriod;

import java.io.IOException;

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
        jsonGenerator.writeString(timeWithPeriod.toString().toLowerCase());
    }
}