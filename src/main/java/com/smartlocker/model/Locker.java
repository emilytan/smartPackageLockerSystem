package com.smartlocker.model;

import java.util.Objects;
import java.util.Optional;

/**
 * One locker compartment. It protects its own rule: at most one parcel at a time,
 * and only a parcel that fits. Not thread-safe by itself; the service coordinates access.
 */
public class Locker {

    private final int id;
    private final LockerSize size;
    private Deposit deposit; // null while the locker is empty

    public Locker(int id, LockerSize size) {
        this.id = id;
        this.size = Objects.requireNonNull(size, "size");
    }

    public int id() {
        return id;
    }

    public LockerSize size() {
        return size;
    }

    public boolean isAvailable() {
        return deposit == null;
    }

    public Optional<Deposit> deposit() {
        return Optional.ofNullable(deposit);
    }

    /** True if this locker holds a parcel and the given code is that parcel's pickup code. */
    public boolean isUnlockedBy(String pickupCode) {
        return deposit != null && deposit.pickupCode().equals(pickupCode);
    }

    /** True if this locker currently holds the parcel with this tracking ID. */
    public boolean holdsParcel(String trackingId) {
        return deposit != null && deposit.parcel().trackingId().equals(trackingId);
    }

    public void store(Deposit newDeposit) {
        Objects.requireNonNull(newDeposit, "deposit");
        if (!isAvailable()) {
            throw new IllegalStateException("Locker " + id + " is already occupied");
        }
        if (!size.isBigEnoughFor(newDeposit.parcel().lockerSizeNeeded())) {
            throw new IllegalArgumentException("Parcel does not fit in a " + size + " locker");
        }
        this.deposit = newDeposit;
    }

    public Deposit release() {
        if (isAvailable()) {
            throw new IllegalStateException("Locker " + id + " is already empty");
        }
        Deposit released = deposit;
        deposit = null;
        return released;
    }
}
