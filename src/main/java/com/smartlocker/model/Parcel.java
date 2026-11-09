package com.smartlocker.model;

import com.smartlocker.exception.InvalidInputException;

/** A parcel handed in by a delivery agent. Dimensions are in cm. */
public record Parcel(String trackingId, String customer, int lengthCm, int widthCm, int heightCm) {

    public Parcel {
        if (trackingId == null || trackingId.isBlank()) {
            throw new InvalidInputException("Tracking ID is required");
        }
        if (customer == null || customer.isBlank()) {
            throw new InvalidInputException("Customer is required");
        }
        if (lengthCm <= 0 || widthCm <= 0 || heightCm <= 0) {
            throw new InvalidInputException("Parcel dimensions must be greater than zero");
        }
    }

    /** The smallest locker size this parcel fits in. Throws if it is too large for every size. */
    public LockerSize lockerSizeNeeded() {
        return LockerSize.smallestFor(lengthCm, widthCm, heightCm);
    }
}
