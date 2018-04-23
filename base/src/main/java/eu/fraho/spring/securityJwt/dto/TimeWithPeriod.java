/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.fraho.spring.securityJwt.service.TimeWithPeriodSerializer;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * This class specifies a quanitity of periods.
 * Used for the configuration to provide some human readable values for the expiration settings.<br>
 * Just specify them like this: {@code fraho.jwt.example: 1 hour} or {@code fraho.jwt.example: 45 minutes}<br>
 * <br>
 * Example values:
 * <ul>
 * <li>1 hour</li>
 * <li>42 seconds</li>
 * <li>1 day</li>
 * <li>7 days</li>
 * <li>1 week</li>
 * </ul>
 *
 * @see #TimeWithPeriod(String)
 * @see #TimeWithPeriod(int, ChronoUnit)
 */
@Value
@Immutable
@Slf4j
@JsonSerialize(using = TimeWithPeriodSerializer.class)
public final class TimeWithPeriod {
    /**
     * Quantity of the {@link #chronoUnit}
     */
    private final int quantity;

    /**
     * The chronoUnit to use
     */
    @NotNull
    @NonNull
    private final ChronoUnit chronoUnit;

    /**
     * Parse the given configuration value and extract the {@link #quantity} and {@link #chronoUnit}.<br>
     *
     * @param value A string representation like &quot;&lt;quantity&gt; &lt;chronoUnit&gt;&quot;
     */
    public TimeWithPeriod(@NotNull final String value) {
        final String[] parts = value.split("\\s", 2);
        String period = parts[1].toUpperCase();
        if (!period.endsWith("S")) {
            period = period + "S";
        }

        quantity = Integer.valueOf(parts[0]);
        chronoUnit = ChronoUnit.valueOf(period);
    }

    /**
     * Create a new instance using the given values.
     *
     * @param quantity   Quantitiy of the chronoUnit
     * @param chronoUnit The chronoUnit to use
     */
    @Builder
    public TimeWithPeriod(int quantity, @NotNull ChronoUnit chronoUnit) {
        this.quantity = quantity;
        this.chronoUnit = chronoUnit;
    }

    /**
     * Convert this objects quantity and chronoUnit to seconds.
     *
     * @return count of seconds
     */
    public long toSeconds() {
        return chronoUnit.getDuration().multipliedBy(quantity).getSeconds();
    }

    public String toString() {
        return String.format("%d %s", quantity, chronoUnit);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(toSeconds());
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof TimeWithPeriod)) {
            return false;
        }
        return (toSeconds() == ((TimeWithPeriod) o).toSeconds());
    }

    public TimeUnit getTimeUnit() {
        switch (chronoUnit) {
            case NANOS:
                return TimeUnit.NANOSECONDS;
            case MICROS:
                return TimeUnit.MICROSECONDS;
            case MILLIS:
                return TimeUnit.MILLISECONDS;
            case SECONDS:
                return TimeUnit.SECONDS;
            case MINUTES:
                return TimeUnit.MINUTES;
            case HOURS:
                return TimeUnit.HOURS;
            case DAYS:
                return TimeUnit.DAYS;
            default:
                throw new IllegalArgumentException("No TimeUnit equivalent for " + chronoUnit);
        }
    }
}