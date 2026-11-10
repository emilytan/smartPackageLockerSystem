package com.smartlocker;

/**
 * The locker ID and pickup code do not match a stored parcel. The message is deliberately the same
 * whether the locker is unknown, empty or the code is wrong, so it reveals nothing to someone guessing.
 */
public class InvalidPickupException extends LockerException {

    private static final long serialVersionUID = 1L;

    public InvalidPickupException() {
        super("Invalid locker ID or pickup code");
    }
}
