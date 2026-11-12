package com.smartlocker.exception;

import com.smartlocker.model.Parcel;

/** A parcel with this tracking ID is already sitting in a locker. */
public class DuplicateParcelException extends LockerException {

    private static final long serialVersionUID = 1L;

    public DuplicateParcelException(String trackingId) {
        super("Parcel " + trackingId + " is already stored in a locker");
    }
}
