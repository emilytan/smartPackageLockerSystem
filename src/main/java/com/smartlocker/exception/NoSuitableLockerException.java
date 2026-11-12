package com.smartlocker.exception;

/** Every locker that could hold the parcel is currently occupied (or none exists). */
public class NoSuitableLockerException extends LockerException {

    private static final long serialVersionUID = 1L;

    public NoSuitableLockerException(String message) {
        super(message);
    }
}
