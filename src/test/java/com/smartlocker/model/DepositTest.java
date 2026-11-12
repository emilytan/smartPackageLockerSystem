package com.smartlocker.model;

import com.smartlocker.model.Deposit;
import com.smartlocker.model.Parcel;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class DepositTest {

    private static final Parcel PARCEL = new Parcel("TRACK-1", "Customer", 10, 10, 10);
    private static final Instant STORED_AT = Instant.parse("2026-01-01T10:00:00Z");

    @Test
    void pickupCodeIsRequired() {
        assertThrows(IllegalArgumentException.class, () -> new Deposit(PARCEL, null, STORED_AT));
        assertThrows(IllegalArgumentException.class, () -> new Deposit(PARCEL, "", STORED_AT));
        assertThrows(IllegalArgumentException.class, () -> new Deposit(PARCEL, "   ", STORED_AT));
    }
}
