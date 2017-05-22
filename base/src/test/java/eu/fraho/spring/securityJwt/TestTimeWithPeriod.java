package eu.fraho.spring.securityJwt;

import eu.fraho.spring.securityJwt.dto.TimeWithPeriod;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class TestTimeWithPeriod {
    @Test
    public void testToString() {
        Assert.assertEquals("1 DAYS", new TimeWithPeriod(1, TimeUnit.DAYS).toString());
        Assert.assertEquals("7 HOURS", new TimeWithPeriod(7, TimeUnit.HOURS).toString());
    }

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
}
