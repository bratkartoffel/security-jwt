/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.ut.dto;

import eu.fraho.spring.securityJwt.dto.TimeWithPeriod;
import org.junit.Assert;
import org.junit.Test;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class TimeWithPeriodTest {
    @Test
    public void testStringConstructor() {
        Assert.assertEquals("1 Days", new TimeWithPeriod("1 day").toString());
        Assert.assertEquals("7 Hours", new TimeWithPeriod("7 hours").toString());
        Assert.assertEquals("2 Months", new TimeWithPeriod("2 months").toString());
    }

    @Test
    public void testToString() {
        Assert.assertEquals("1 Days", new TimeWithPeriod(1, ChronoUnit.DAYS).toString());
        Assert.assertEquals("7 Hours", new TimeWithPeriod(7, ChronoUnit.HOURS).toString());
        Assert.assertEquals("2 Months", new TimeWithPeriod(2, ChronoUnit.MONTHS).toString());
    }

    @Test
    public void testEquals() {
        Assert.assertEquals(new TimeWithPeriod(1, ChronoUnit.DAYS), new TimeWithPeriod(24, ChronoUnit.HOURS));
        Assert.assertNotEquals(new TimeWithPeriod(1, ChronoUnit.HOURS), new TimeWithPeriod(3601, ChronoUnit.SECONDS));
        Assert.assertNotEquals(new TimeWithPeriod(1, ChronoUnit.HOURS), null);
        Assert.assertNotEquals(new TimeWithPeriod(1, ChronoUnit.HOURS), new Object());
    }

    @Test
    public void testToSeconds() {
        Assert.assertEquals(3_600, new TimeWithPeriod(1, ChronoUnit.HOURS).toSeconds());
        Assert.assertEquals(86_400, new TimeWithPeriod(1, ChronoUnit.DAYS).toSeconds());
    }

    @Test
    public void testToMillis() {
        Assert.assertEquals(60_000, new TimeWithPeriod(1, ChronoUnit.MINUTES).toMillis());
    }

    @Test
    public void testGetTimeUnit() {
        Assert.assertEquals(TimeUnit.NANOSECONDS, new TimeWithPeriod(1, ChronoUnit.NANOS).getTimeUnit());
        Assert.assertEquals(TimeUnit.MICROSECONDS, new TimeWithPeriod(1, ChronoUnit.MICROS).getTimeUnit());
        Assert.assertEquals(TimeUnit.MILLISECONDS, new TimeWithPeriod(1, ChronoUnit.MILLIS).getTimeUnit());
        Assert.assertEquals(TimeUnit.SECONDS, new TimeWithPeriod(1, ChronoUnit.SECONDS).getTimeUnit());
        Assert.assertEquals(TimeUnit.MINUTES, new TimeWithPeriod(1, ChronoUnit.MINUTES).getTimeUnit());
        Assert.assertEquals(TimeUnit.HOURS, new TimeWithPeriod(1, ChronoUnit.HOURS).getTimeUnit());
        Assert.assertEquals(TimeUnit.DAYS, new TimeWithPeriod(1, ChronoUnit.DAYS).getTimeUnit());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTimeUnitUnknown() {
        try {
            Assert.assertNull(TimeWithPeriod.builder().chronoUnit(ChronoUnit.HALF_DAYS).quantity(1).build().getTimeUnit());
        } catch (IllegalArgumentException iae) {
            Assert.assertEquals("No TimeUnit equivalent for HalfDays", iae.getMessage());
            throw iae;
        }
    }

    @Test
    public void testHashcode() {
        Assert.assertEquals(new TimeWithPeriod(60, ChronoUnit.MINUTES).hashCode(), new TimeWithPeriod(1, ChronoUnit.HOURS).hashCode());
        Assert.assertNotEquals(new TimeWithPeriod(1_000_000, ChronoUnit.SECONDS).hashCode(), new TimeWithPeriod(1_000_001, ChronoUnit.SECONDS).toSeconds());
    }

    @Test
    public void testValues() {
        TimeWithPeriod testee = new TimeWithPeriod(60, ChronoUnit.MINUTES);
        Assert.assertEquals(60, testee.getQuantity());
        Assert.assertEquals(ChronoUnit.MINUTES, testee.getChronoUnit());
    }
}
