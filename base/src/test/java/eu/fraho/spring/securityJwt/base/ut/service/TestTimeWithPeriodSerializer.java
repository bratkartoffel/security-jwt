/*
 * MIT Licence
 * Copyright (c) 2018 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.ut.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import eu.fraho.spring.securityJwt.base.dto.TimeWithPeriod;
import eu.fraho.spring.securityJwt.base.service.TimeWithPeriodSerializer;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.time.temporal.ChronoUnit;

public class TestTimeWithPeriodSerializer {
    protected TimeWithPeriodSerializer getInstance() {
        return new TimeWithPeriodSerializer();
    }

    @Test
    public void testSerialize() throws IOException {
        TimeWithPeriodSerializer instance = getInstance();
        JsonGenerator jsonGenerator = Mockito.mock(JsonGenerator.class);
        SerializerProvider serializerProvider = Mockito.mock(SerializerProvider.class);
        TimeWithPeriod testee = new TimeWithPeriod(42, ChronoUnit.DAYS);

        instance.serialize(testee, jsonGenerator, serializerProvider);
        Mockito.verify(jsonGenerator).writeString(Mockito.eq("42 days"));
        Mockito.verifyZeroInteractions(serializerProvider);
    }
}
