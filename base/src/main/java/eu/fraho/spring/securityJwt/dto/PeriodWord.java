/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.dto;

import lombok.Getter;

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
@SuppressWarnings("unused")
@Getter
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
     */
    private final TimeUnit timeUnit;

    PeriodWord(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }
}
