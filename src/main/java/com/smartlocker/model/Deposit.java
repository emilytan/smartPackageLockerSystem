package com.smartlocker.model;

import java.time.Instant;
import java.util.Objects;

/** What an occupied locker holds: the parcel that was put in, its pickup code, and when it was stored. */
public record Deposit(Parcel parcel, String pickupCode, Instant storedAt) {

    public Deposit {
        Objects.requireNonNull(parcel, "parcel");
        if (pickupCode == null || pickupCode.isBlank()) {
            throw new IllegalArgumentException("Pickup code is required");
        }
        Objects.requireNonNull(storedAt, "storedAt");
    }
}
