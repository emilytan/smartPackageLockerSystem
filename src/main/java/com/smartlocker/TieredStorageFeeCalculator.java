package com.smartlocker;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Calculates fees using 24-hour periods measured from the parcel's storage instant.
 * Only completed 24-hour periods count as billable days. The base daily rate is X:
 * X for days 1-5, 2X for days 6-10, and 3X for every subsequent day.
 */
public final class TieredStorageFeeCalculator {

    private static final Duration DAY = Duration.ofHours(24);
    private static final long FIRST_TIER_DAYS = 5;
    private static final long SECOND_TIER_DAYS = 5;

    private final BigDecimal baseDailyRate;

    public TieredStorageFeeCalculator(BigDecimal baseDailyRate) {
        this.baseDailyRate = Objects.requireNonNull(baseDailyRate, "baseDailyRate");
        if (baseDailyRate.signum() <= 0) {
            throw new IllegalArgumentException("Base daily rate must be greater than zero");
        }
    }

    public BigDecimal calculate(Instant storedAt, Instant retrievedAt) {
        Objects.requireNonNull(storedAt, "storedAt");
        Objects.requireNonNull(retrievedAt, "retrievedAt");

        Duration elapsed = Duration.between(storedAt, retrievedAt);
        if (elapsed.isNegative()) {
            throw new IllegalArgumentException("Retrieval time cannot be before storage time");
        }
        if (elapsed.isZero()) {
            return BigDecimal.ZERO;
        }

        // Count completed 24-hour periods from storedAt, not calendar-day boundaries.
        long days = elapsed.dividedBy(DAY);

        long firstTierDays = Math.min(days, FIRST_TIER_DAYS);
        long secondTierDays = Math.min(Math.max(days - FIRST_TIER_DAYS, 0), SECOND_TIER_DAYS);
        long thirdTierDays = Math.max(days - FIRST_TIER_DAYS - SECOND_TIER_DAYS, 0);

        return baseDailyRate.multiply(BigDecimal.valueOf(firstTierDays))
                .add(baseDailyRate.multiply(BigDecimal.valueOf(2L * secondTierDays)))
                .add(baseDailyRate.multiply(BigDecimal.valueOf(3L * thirdTierDays)));
    }
}
