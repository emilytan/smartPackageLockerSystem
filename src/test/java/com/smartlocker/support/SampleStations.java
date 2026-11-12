package com.smartlocker.support;

import com.smartlocker.codegenerator.RandomPickupCodeGenerator;
import com.smartlocker.feecalculator.TieredStorageFeeCalculator;
import com.smartlocker.result.LockerStatus;
import com.smartlocker.service.LockerService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/** Ready-made locker stations for tests, plus the pickup codes the tests refer to. */
public final class SampleStations {

    /** A clock that always says the same time, so tests do not depend on when they run. */
    public static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);

    // The codes a test station hands out, in order, when a test needs to know them.
    public static final String FIRST_CODE = "482913";
    public static final String SECOND_CODE = "715204";
    public static final String WRONG_CODE = "000000";

    private SampleStations() {}

    /** A station whose code generator hands out exactly these codes, in this order. */
    public static LockerService stationHandingOutCodes(String... codes) {
        Iterator<String> nextCode = Arrays.asList(codes).iterator();
        return new LockerService(new RandomPickupCodeGenerator() {
            @Override
            public String next() {
                return nextCode.next();
            }
        }, CLOCK, new TieredStorageFeeCalculator(BigDecimal.ONE));
    }

    /** A station that never runs out of codes (100001, 100002, ...) for tests that don't care about them. */
    public static LockerService station() {
        AtomicInteger counter = new AtomicInteger(100_000);
        return new LockerService(new RandomPickupCodeGenerator() {
            @Override
            public String next() {
                return String.valueOf(counter.incrementAndGet());
            }
        }, CLOCK, new TieredStorageFeeCalculator(BigDecimal.ONE));
    }

    public static LockerStatus statusOf(LockerService station, int lockerId) {
        for (LockerStatus status : station.listLockers()) {
            if (status.id() == lockerId) {
                return status;
            }
        }
        throw new AssertionError("No locker " + lockerId);
    }
}
