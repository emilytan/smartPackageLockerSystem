package com.smartlocker;

import java.time.Instant;
import java.util.Objects;

/** What an occupied locker holds: the parcel that was put in, its pickup code, and when it was stored. */
public record Deposit(Parcel parcel, String pickupCode, Instant storedAt) {

    public Deposit {
        Objects.requireNonNull(parcel, "parcel");
        Objects.requireNonNull(pickupCode, "pickupCode");
        Objects.requireNonNull(storedAt, "storedAt");
    }
}
