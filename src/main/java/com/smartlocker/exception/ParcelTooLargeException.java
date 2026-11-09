package com.smartlocker.exception;

/** The parcel is bigger than even the largest locker size, so it can never be stored. */
public class ParcelTooLargeException extends LockerException {

    public ParcelTooLargeException(String message) {
        super(message);
    }
}
