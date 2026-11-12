package com.smartlocker.result;

import java.math.BigDecimal;
import java.util.Objects;

/** Confirmation that a parcel was collected, including its calculated storage fee. */
public record PickupReceipt(String trackingId, int lockerId, BigDecimal storageFee) {

    public PickupReceipt {
        Objects.requireNonNull(trackingId, "trackingId");
        Objects.requireNonNull(storageFee, "storageFee");
        if (storageFee.signum() < 0) {
            throw new IllegalArgumentException("Storage fee cannot be negative");
        }
    }
}
