package com.smartlocker.contract;

import java.math.BigDecimal;
import java.time.Instant;

/** Calculates the storage fee for a parcel between its storage and retrieval instants. */
@FunctionalInterface
public interface StorageFeeCalculator {

    BigDecimal calculate(Instant storedAt, Instant retrievedAt);
}
