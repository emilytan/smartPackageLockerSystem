package com.smartlocker.exception;

/** The caller supplied missing or nonsensical input (blank tracking ID, zero-length parcel, ...). */
public class InvalidInputException extends LockerException {

    public InvalidInputException(String message) {
        super(message);
    }
}
