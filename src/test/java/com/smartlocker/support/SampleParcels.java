package com.smartlocker.support;

import com.smartlocker.Parcel;

/**
 * Ready-made parcels for tests, each named after a recognisable person and item.
 * The comment on each one says which locker size it needs.
 */
public final class SampleParcels {

    private SampleParcels() {}

    public static Parcel alicesPhoneCase() { // SMALL
        return new Parcel("SPX-1001", "Alice Tan", 20, 12, 4);
    }

    public static Parcel bensPaperbacks() { // SMALL
        return new Parcel("SPX-1002", "Ben Lim", 25, 18, 6);
    }

    public static Parcel carolsEarbuds() { // SMALL
        return new Parcel("SPX-1003", "Carol Wong", 12, 10, 5);
    }

    public static Parcel davidsShoeBox() { // MEDIUM
        return new Parcel("SPX-2001", "David Raj", 35, 25, 15);
    }

    public static Parcel evasKettle() { // LARGE
        return new Parcel("SPX-3001", "Eva Chong", 30, 25, 25);
    }

    public static Parcel franksOfficeChair() { // too large for every locker
        return new Parcel("SPX-9001", "Frank Ong", 90, 60, 50);
    }
}
