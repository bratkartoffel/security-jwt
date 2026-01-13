/*
 * MIT Licence
 * Copyright (c) 2026 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.tests.ut.dto;

import eu.fraho.spring.securityJwt.base.dto.TimeWithPeriod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class TimeWithPeriodTest {
    @Test
    public void testStringConstructor() {
        Assertions.assertEquals("1 Days", new TimeWithPeriod("1 day").toString());
        Assertions.assertEquals("7 Hours", new TimeWithPeriod("7 hours").toString());
        Assertions.assertEquals("2 Months", new TimeWithPeriod("2 months").toString());
    }

    @Test
    public void testToString() {
        Assertions.assertEquals("1 Days", new TimeWithPeriod(1, ChronoUnit.DAYS).toString());
        Assertions.assertEquals("7 Hours", new TimeWithPeriod(7, ChronoUnit.HOURS).toString());
        Assertions.assertEquals("2 Months", new TimeWithPeriod(2, ChronoUnit.MONTHS).toString());
    }

    @Test
    public void testEquals() {
        Assertions.assertEquals(new TimeWithPeriod(1, ChronoUnit.DAYS), new TimeWithPeriod(24, ChronoUnit.HOURS));
        Assertions.assertNotEquals(new TimeWithPeriod(1, ChronoUnit.HOURS), new TimeWithPeriod(3601, ChronoUnit.SECONDS));
        Assertions.assertNotEquals(new TimeWithPeriod(1, ChronoUnit.HOURS), null);
        Assertions.assertNotEquals(new TimeWithPeriod(1, ChronoUnit.HOURS), new Object());
    }

    @Test
    public void testToSeconds() {
        Assertions.assertEquals(3_600, new TimeWithPeriod(1, ChronoUnit.HOURS).toSeconds());
        Assertions.assertEquals(86_400, new TimeWithPeriod(1, ChronoUnit.DAYS).toSeconds());
    }

    @Test
    public void testToMillis() {
        Assertions.assertEquals(60_000, new TimeWithPeriod(1, ChronoUnit.MINUTES).toMillis());
    }

    @Test
    public void testGetTimeUnit() {
        Assertions.assertEquals(TimeUnit.NANOSECONDS, new TimeWithPeriod(1, ChronoUnit.NANOS).getTimeUnit());
        Assertions.assertEquals(TimeUnit.MICROSECONDS, new TimeWithPeriod(1, ChronoUnit.MICROS).getTimeUnit());
        Assertions.assertEquals(TimeUnit.MILLISECONDS, new TimeWithPeriod(1, ChronoUnit.MILLIS).getTimeUnit());
        Assertions.assertEquals(TimeUnit.SECONDS, new TimeWithPeriod(1, ChronoUnit.SECONDS).getTimeUnit());
        Assertions.assertEquals(TimeUnit.MINUTES, new TimeWithPeriod(1, ChronoUnit.MINUTES).getTimeUnit());
        Assertions.assertEquals(TimeUnit.HOURS, new TimeWithPeriod(1, ChronoUnit.HOURS).getTimeUnit());
        Assertions.assertEquals(TimeUnit.DAYS, new TimeWithPeriod(1, ChronoUnit.DAYS).getTimeUnit());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testGetTimeUnitUnknown() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> TimeWithPeriod.builder().chronoUnit(ChronoUnit.HALF_DAYS).quantity(1).build().getTimeUnit());
    }

    @Test
    public void testHashcode() {
        Assertions.assertEquals(new TimeWithPeriod(60, ChronoUnit.MINUTES).hashCode(), new TimeWithPeriod(1, ChronoUnit.HOURS).hashCode());
        Assertions.assertNotEquals(new TimeWithPeriod(1_000_000, ChronoUnit.SECONDS).hashCode(), new TimeWithPeriod(1_000_001, ChronoUnit.SECONDS).toSeconds());
    }

    @Test
    public void testValues() {
        TimeWithPeriod testee = new TimeWithPeriod(60, ChronoUnit.MINUTES);
        Assertions.assertEquals(60, testee.getQuantity());
        Assertions.assertEquals(ChronoUnit.MINUTES, testee.getChronoUnit());
    }
}
