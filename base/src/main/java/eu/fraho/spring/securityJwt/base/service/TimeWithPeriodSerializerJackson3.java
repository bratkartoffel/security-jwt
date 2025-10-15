/*
 * MIT Licence
 * Copyright (c) 2025 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.service;

import eu.fraho.spring.securityJwt.base.dto.TimeWithPeriod;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public final class TimeWithPeriodSerializerJackson3 extends StdSerializer<TimeWithPeriod> {
    @SuppressWarnings("unused")
    public TimeWithPeriodSerializerJackson3() {
        this(null);
    }

    public TimeWithPeriodSerializerJackson3(Class<TimeWithPeriod> t) {
        super(t);
    }

    @Override
    public void serialize(TimeWithPeriod value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
        gen.writeString(value.toString().toLowerCase());
    }
}