package com.smartlocker;

/** The parcel is bigger than even the largest locker size, so it can never be stored. */
public class ParcelTooLargeException extends LockerException {

    private static final long serialVersionUID = 1L;

    public ParcelTooLargeException(String message) {
        super(message);
    }
}
