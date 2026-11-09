package com.smartlocker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.smartlocker.implementation.TieredStorageFeeCalculator;
import com.smartlocker.model.LockerSize;
import com.smartlocker.model.Parcel;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class StorageFeeRetrievalTest {

    @Test
    void returnsCalculatedFeeAndMakesLockerAvailableAfterRetrieval() {
        Instant storedAt = Instant.parse("2026-01-01T10:00:00Z");
        AdjustableClock clock = new AdjustableClock(storedAt);
        LockerService service = new LockerService(
                () -> "482913", clock, new TieredStorageFeeCalculator(new BigDecimal("1.00")));
        service.createLocker(LockerSize.SMALL);
        service.storeParcel(new Parcel("TRACK-1", "Customer", 10, 10, 10));

        clock.advance(Duration.ofDays(6));
        var receipt = service.retrieveParcel(1, "482913");

        assertEquals("TRACK-1", receipt.trackingId());
        assertEquals(1, receipt.lockerId());
        assertEquals(new BigDecimal("7.00"), receipt.storageFee());
        assertTrue(service.listLockers().get(0).available());
    }

    private static final class AdjustableClock extends Clock {
        private final AtomicReference<Instant> instant;

        private AdjustableClock(Instant initialInstant) {
            this.instant = new AtomicReference<>(initialInstant);
        }

        void advance(Duration duration) {
            instant.updateAndGet(current -> current.plus(duration));
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            if (!ZoneOffset.UTC.equals(zone)) {
                throw new UnsupportedOperationException("Test clock only supports UTC");
            }
            return this;
        }

        @Override
        public Instant instant() {
            return instant.get();
        }
    }
}
