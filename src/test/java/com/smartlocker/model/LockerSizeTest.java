package com.smartlocker.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.smartlocker.exception.ParcelTooLargeException;
import org.junit.jupiter.api.Test;

class LockerSizeTest {

    // ---- Which size does a parcel need? ----

    @Test
    void smallParcelNeedsSmallLocker() {
        assertEquals(LockerSize.SMALL, LockerSize.smallestFor(30, 20, 8));
    }

    @Test
    void parcelExactlyAtSmallLimitStillFitsSmall() {
        assertEquals(LockerSize.SMALL, LockerSize.smallestFor(40, 30, 10));
    }

    @Test
    void oneCentimetreOverSmallLimitNeedsMedium() {
        assertEquals(LockerSize.MEDIUM, LockerSize.smallestFor(40, 30, 11));
    }

    @Test
    void parcelBetweenSmallAndMediumLimitsNeedsMedium() {
        assertEquals(LockerSize.MEDIUM, LockerSize.smallestFor(35, 25, 20));
    }

    @Test
    void parcelOverMediumLimitNeedsLarge() {
        assertEquals(LockerSize.LARGE, LockerSize.smallestFor(40, 30, 21));
    }

    @Test
    void parcelExactlyAtLargeLimitNeedsLarge() {
        assertEquals(LockerSize.LARGE, LockerSize.smallestFor(40, 30, 30));
    }

    @Test
    void parcelCanBeTurnedAnyWay() {
        assertEquals(LockerSize.SMALL, LockerSize.smallestFor(10, 40, 30));
        assertEquals(LockerSize.SMALL, LockerSize.smallestFor(30, 10, 40));
    }

    @Test
    void parcelLongerThanTheLargestLockerIsTooLarge() {
        assertThrows(ParcelTooLargeException.class, () -> LockerSize.smallestFor(41, 10, 5));
    }

    @Test
    void parcelTooLargeInAnyOtherDimensionIsRejected() {
        assertThrows(ParcelTooLargeException.class, () -> LockerSize.smallestFor(40, 31, 5));
        assertThrows(ParcelTooLargeException.class, () -> LockerSize.smallestFor(40, 30, 31));
    }

    @Test
    void tooLargeMessageShowsTheParcelDimensions() {
        ParcelTooLargeException e =
                assertThrows(ParcelTooLargeException.class, () -> LockerSize.smallestFor(50, 20, 10));
        assertTrue(e.getMessage().contains("50 x 20 x 10"));
    }

    // ---- Can a locker of one size hold a parcel that needs another size? ----

    @Test
    void lockerAcceptsParcelsOfItsOwnSizeOrSmaller() {
        assertTrue(LockerSize.MEDIUM.isBigEnoughFor(LockerSize.MEDIUM));
        assertTrue(LockerSize.LARGE.isBigEnoughFor(LockerSize.SMALL));
    }

    @Test
    void lockerRejectsParcelsThatNeedABiggerSize() {
        assertFalse(LockerSize.SMALL.isBigEnoughFor(LockerSize.MEDIUM));
        assertFalse(LockerSize.MEDIUM.isBigEnoughFor(LockerSize.LARGE));
    }
}
