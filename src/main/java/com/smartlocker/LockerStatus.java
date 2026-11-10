package com.smartlocker;

/** A read-only snapshot of one locker, so callers can never change locker state directly. */
public record LockerStatus(int id, LockerSize size, boolean available) {

    public static LockerStatus of(Locker locker) {
        return new LockerStatus(locker.id(), locker.size(), locker.isAvailable());
    }
}
