package com.smartlocker.exception;

/** Base type for every business error the locker system reports to its caller. */
public class LockerException extends RuntimeException {

    public LockerException(String message) {
        super(message);
    }
}
