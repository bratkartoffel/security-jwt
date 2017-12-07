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

import java.util.concurrent.TimeUnit;

public class TimeWithPeriodTest {
    @Test
    public void testStringConstructor() {
        Assert.assertEquals("1 DAYS", new TimeWithPeriod("1 day").toString());
        Assert.assertEquals("7 HOURS", new TimeWithPeriod("7 hours").toString());
    }

    @Test
    public void testToString() {
        Assert.assertEquals("1 DAYS", new TimeWithPeriod(1, TimeUnit.DAYS).toString());
        Assert.assertEquals("7 HOURS", new TimeWithPeriod(7, TimeUnit.HOURS).toString());
    }

    @SuppressWarnings("ObjectEqualsNull") // is intended; check for correct null handling within "equals"
    @Test
    public void testEquals() {
        Assert.assertEquals(new TimeWithPeriod(1, TimeUnit.DAYS), new TimeWithPeriod(24, TimeUnit.HOURS));
        Assert.assertNotEquals(new TimeWithPeriod(1, TimeUnit.HOURS), new TimeWithPeriod(3601, TimeUnit.SECONDS));
        Assert.assertNotEquals(new TimeWithPeriod(1, TimeUnit.HOURS), null);
        Assert.assertNotEquals(new TimeWithPeriod(1, TimeUnit.HOURS), new Object());
    }

    @Test
    public void testToSeconds() {
        Assert.assertEquals(3600, new TimeWithPeriod(1, TimeUnit.HOURS).toSeconds());
        Assert.assertEquals(86_400, new TimeWithPeriod(1, TimeUnit.DAYS).toSeconds());
    }

    @Test
    public void testHashcode() {
        Assert.assertEquals(new TimeWithPeriod(60, TimeUnit.MINUTES).hashCode(), new TimeWithPeriod(1, TimeUnit.HOURS).hashCode());
        Assert.assertNotEquals(new TimeWithPeriod(1_000_000, TimeUnit.SECONDS).hashCode(), new TimeWithPeriod(1_000_001, TimeUnit.SECONDS).toSeconds());
    }

    @Test
    public void testValues() {
        TimeWithPeriod testee = new TimeWithPeriod(60, TimeUnit.MINUTES);
        Assert.assertEquals(60, testee.getQuantity());
        Assert.assertEquals(TimeUnit.MINUTES, testee.getTimeUnit());
    }
}
