package com.smartlocker.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class TieredStorageFeeCalculatorTest {

    private static final Instant STORED_AT = Instant.parse("2026-01-01T10:00:00Z");
    private final TieredStorageFeeCalculator calculator =
            new TieredStorageFeeCalculator(new BigDecimal("1.00"));

    @Test
    void chargesNothingWhenRetrievedAtStorageTime() {
        assertFee("0", Duration.ZERO);
    }

    @Test
    void countsOnlyCompleted24HourPeriodsFromStorageTime() {
        assertFee("0.00", Duration.ofHours(1));
        assertFee("0.00", Duration.ofHours(23));
        assertFee("1.00", Duration.ofHours(24));
        assertFee("1.00", Duration.ofHours(24).plusSeconds(1));
        assertFee("2.00", Duration.ofHours(48));
    }

    @Test
    void appliesFirstTierThroughFive24HourDays() {
        assertFee("5.00", Duration.ofDays(5));
        assertFee("5.00", Duration.ofDays(5).plusSeconds(1));
    }

    @Test
    void appliesSecondTierAfterFiveDaysThroughTenDays() {
        assertFee("7.00", Duration.ofDays(6));
        assertFee("15.00", Duration.ofDays(10));
        assertFee("15.00", Duration.ofDays(10).plusSeconds(1));
    }

    @Test
    void appliesThirdTierAfterTenDays() {
        assertFee("18.00", Duration.ofDays(11));
        assertFee("30.00", Duration.ofDays(15));
    }

    @Test
    void rejectsRetrievalBeforeStorage() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculate(STORED_AT, STORED_AT.minusSeconds(1)));
    }

    @Test
    void rejectsNonPositiveBaseRate() {
        assertThrows(IllegalArgumentException.class,
                () -> new TieredStorageFeeCalculator(BigDecimal.ZERO));
    }

    private void assertFee(String expected, Duration elapsed) {
        assertEquals(new BigDecimal(expected),
                calculator.calculate(STORED_AT, STORED_AT.plus(elapsed)));
    }
}
