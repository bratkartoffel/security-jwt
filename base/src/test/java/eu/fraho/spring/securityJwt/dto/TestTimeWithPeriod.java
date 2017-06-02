/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.dto;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class TestTimeWithPeriod {
    @Test
    public void testToString() {
        Assert.assertEquals("1 DAYS", new TimeWithPeriod(1, TimeUnit.DAYS).toString());
        Assert.assertEquals("7 HOURS", new TimeWithPeriod(7, TimeUnit.HOURS).toString());
    }

    @SuppressWarnings("ObjectEqualsNull") // is intended; check for correct null handling within "equals"
    @Test
    public void testEquals() {
        Assert.assertTrue(new TimeWithPeriod(1, TimeUnit.DAYS).equals(new TimeWithPeriod(24, TimeUnit.HOURS)));
        Assert.assertFalse(new TimeWithPeriod(1, TimeUnit.HOURS).equals(new TimeWithPeriod(3601, TimeUnit.SECONDS)));
        Assert.assertFalse(new TimeWithPeriod(1, TimeUnit.HOURS).equals(null));
        Assert.assertFalse(new TimeWithPeriod(1, TimeUnit.HOURS).equals(new Object()));
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
