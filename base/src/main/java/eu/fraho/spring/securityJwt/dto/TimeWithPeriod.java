/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.dto;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.Immutable;

import java.util.concurrent.TimeUnit;

/**
 * This class specifies a quanitity of periods.
 * Used for the configuration to provide some human readable values for the expiration settings.
 *
 * @see #TimeWithPeriod(String)
 * @see #TimeWithPeriod(int, TimeUnit)
 */
@Value
@Slf4j
@Immutable
public class TimeWithPeriod {
    /**
     * Quantity of the {@link #timeUnit}
     */
    private int quantity;

    /**
     * The timeUnit to use
     */
    private TimeUnit timeUnit;

    /**
     * Parse the given configuration value and extract the {@link #quantity} and {@link #timeUnit}.<br>
     * Example values:
     * <ul>
     * <li>1 hour</li>
     * <li>42 seconds</li>
     * <li>1 day</li>
     * <li>7 days</li>
     * </ul>
     *
     * @param value A string representation like &quot;&lt;quantity&gt; &lt;timeUnit&gt;&quot;
     */
    public TimeWithPeriod(final String value) {
        final String[] parts = value.split("\\s", 2);

        quantity = Integer.valueOf(parts[0]);
        timeUnit = PeriodWord.valueOf(parts[1]).getTimeUnit();
    }

    /**
     * Create a new instance using the given values.
     *
     * @param quantity Quantitiy of the timeUnit
     * @param timeUnit The timeUnit to use
     */
    public TimeWithPeriod(int quantity, TimeUnit timeUnit) {
        this.quantity = quantity;
        this.timeUnit = timeUnit;
    }

    /**
     * Convert this objects quantity and timeUnit to seconds.
     *
     * @return count of seconds
     */
    public int toSeconds() {
        return (int) timeUnit.toSeconds(quantity);
    }

    public String toString() {
        return String.format("%d %s", quantity, timeUnit);
    }

    @Override
    public int hashCode() {
        return toSeconds();
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof TimeWithPeriod)) {
            return false;
        }
        return (toSeconds() == ((TimeWithPeriod) o).toSeconds());
    }
}
