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

public class TimeWithPeriodTest {
    @Test
    public void testStringConstructor() {
        Assert.assertEquals("1 Days", new TimeWithPeriod("1 day").toString());
        Assert.assertEquals("7 Hours", new TimeWithPeriod("7 hours").toString());
    }

    @Test
    public void testToString() {
        Assert.assertEquals("1 Days", new TimeWithPeriod(1, ChronoUnit.DAYS).toString());
        Assert.assertEquals("7 Hours", new TimeWithPeriod(7, ChronoUnit.HOURS).toString());
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
        Assert.assertEquals(3600, new TimeWithPeriod(1, ChronoUnit.HOURS).toSeconds());
        Assert.assertEquals(86_400, new TimeWithPeriod(1, ChronoUnit.DAYS).toSeconds());
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
