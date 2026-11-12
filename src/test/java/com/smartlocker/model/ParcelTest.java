package com.smartlocker.model;

import com.smartlocker.exception.InvalidInputException;
import com.smartlocker.exception.ParcelTooLargeException;
import com.smartlocker.model.LockerSize;
import com.smartlocker.model.Parcel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ParcelTest {

    @Test
    void parcelKeepsItsDetails() {
        Parcel parcel = new Parcel("SPX-1001", "Alice Tan", 30, 20, 8);

        assertEquals("SPX-1001", parcel.trackingId());
        assertEquals("Alice Tan", parcel.customer());
        assertEquals(30, parcel.lengthCm());
        assertEquals(20, parcel.widthCm());
        assertEquals(8, parcel.heightCm());
    }

    @Test
    void trackingIdIsRequired() {
        assertThrows(InvalidInputException.class, () -> new Parcel(null, "Alice Tan", 30, 20, 8));
        assertThrows(InvalidInputException.class, () -> new Parcel("", "Alice Tan", 30, 20, 8));
        assertThrows(InvalidInputException.class, () -> new Parcel("   ", "Alice Tan", 30, 20, 8));
    }

    @Test
    void customerIsRequired() {
        assertThrows(InvalidInputException.class, () -> new Parcel("SPX-1001", null, 30, 20, 8));
        assertThrows(InvalidInputException.class, () -> new Parcel("SPX-1001", "", 30, 20, 8));
        assertThrows(InvalidInputException.class, () -> new Parcel("SPX-1001", "   ", 30, 20, 8));
    }

    @Test
    void dimensionsMustBePositive() {
        assertThrows(InvalidInputException.class, () -> new Parcel("SPX-1001", "Alice Tan", 0, 20, 8));
        assertThrows(InvalidInputException.class, () -> new Parcel("SPX-1001", "Alice Tan", 30, -1, 8));
        assertThrows(InvalidInputException.class, () -> new Parcel("SPX-1001", "Alice Tan", 30, 20, 0));
    }

    @Test
    void lockerSizeNeededIsWorkedOutFromTheDimensions() {
        assertEquals(LockerSize.MEDIUM, new Parcel("SPX-1001", "Alice Tan", 35, 25, 20).lockerSizeNeeded());
    }

    @Test
    void oversizedParcelCanBeCreatedButHasNoSize() {
        Parcel parcel = new Parcel("SPX-1001", "Alice Tan", 50, 10, 10);

        assertThrows(ParcelTooLargeException.class, parcel::lockerSizeNeeded);
    }
}
