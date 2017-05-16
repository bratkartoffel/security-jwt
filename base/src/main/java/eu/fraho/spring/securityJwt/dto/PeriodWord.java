package eu.fraho.spring.securityJwt.dto;

import java.util.concurrent.TimeUnit;

/**
 * Word suffixes for the {@link TimeWithPeriod} class values.
 *
 * @see #second
 * @see #seconds
 * @see #minute
 * @see #minutes
 * @see #hour
 * @see #hours
 * @see #day
 * @see #days
 */
enum PeriodWord {
    second(TimeUnit.SECONDS),
    seconds(TimeUnit.SECONDS),
    minute(TimeUnit.MINUTES),
    minutes(TimeUnit.MINUTES),
    hour(TimeUnit.HOURS),
    hours(TimeUnit.HOURS),
    day(TimeUnit.DAYS),
    days(TimeUnit.DAYS);

    /**
     * The backing timeunit (used for conversion to seconds)
     *
     * @see #toSeconds(int)
     */
    private final TimeUnit timeUnit;

    PeriodWord(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public int toSeconds(int value) {
        return (int) timeUnit.toSeconds(value);
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
}
