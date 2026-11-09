package com.smartlocker.exception;

/** Every locker that could hold the parcel is currently occupied (or none exists). */
public class NoSuitableLockerException extends LockerException {

    public NoSuitableLockerException(String message) {
        super(message);
    }
}
