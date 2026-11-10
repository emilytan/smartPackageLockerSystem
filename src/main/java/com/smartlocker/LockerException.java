package com.smartlocker;

/** Base type for every business error the locker system reports to its caller. */
public class LockerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public LockerException(String message) {
        super(message);
    }
}
