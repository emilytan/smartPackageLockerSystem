package com.smartlocker.model;

import static com.smartlocker.support.SampleParcels.alicesPhoneCase;
import static com.smartlocker.support.SampleParcels.bensPaperbacks;
import static com.smartlocker.support.SampleParcels.davidsShoeBox;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class LockerTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");
    private static final String PICKUP_CODE = "482913";

    private static Deposit depositOf(Parcel parcel) {
        return new Deposit(parcel, PICKUP_CODE, NOW);
    }

    @Test
    void newLockerIsEmptyAndAvailable() {
        Locker locker = new Locker(1, LockerSize.SMALL);

        assertEquals(1, locker.id());
        assertEquals(LockerSize.SMALL, locker.size());
        assertTrue(locker.isAvailable());
        assertTrue(locker.deposit().isEmpty());
    }

    @Test
    void storingAParcelMakesTheLockerUnavailable() {
        // Given an empty small locker
        Locker locker = new Locker(1, LockerSize.SMALL);
        Deposit deposit = depositOf(alicesPhoneCase());

        // When Alice's phone case is stored
        locker.store(deposit);

        // Then the locker is taken and holds her parcel
        assertFalse(locker.isAvailable());
        assertEquals(deposit, locker.deposit().get());
    }

    @Test
    void lockerHoldsOnlyOneParcelAtATime() {
        // Given a locker already holding Alice's phone case
        Locker locker = new Locker(1, LockerSize.LARGE);
        Deposit alices = depositOf(alicesPhoneCase());
        locker.store(alices);

        // When Ben's paperbacks are put in the same locker
        // Then it is refused and Alice's parcel is still there
        assertThrows(IllegalStateException.class, () -> locker.store(depositOf(bensPaperbacks())));
        assertEquals(alices, locker.deposit().get());
    }

    @Test
    void parcelThatNeedsABiggerSizeIsRejected() {
        // Given a small locker
        Locker smallLocker = new Locker(1, LockerSize.SMALL);

        // When David's shoe box (needs MEDIUM) is put in it
        // Then it is refused and the locker stays empty
        assertThrows(IllegalArgumentException.class, () -> smallLocker.store(depositOf(davidsShoeBox())));
        assertTrue(smallLocker.isAvailable());
    }

    @Test
    void releaseReturnsTheDepositAndFreesTheLocker() {
        Locker locker = new Locker(1, LockerSize.SMALL);
        Deposit deposit = depositOf(alicesPhoneCase());
        locker.store(deposit);

        Deposit released = locker.release();

        assertEquals(deposit, released);
        assertTrue(locker.isAvailable());
    }

    @Test
    void releasingAnEmptyLockerFails() {
        Locker locker = new Locker(1, LockerSize.SMALL);

        assertThrows(IllegalStateException.class, locker::release);
    }

    @Test
    void lockerCanBeReusedAfterRelease() {
        Locker locker = new Locker(1, LockerSize.SMALL);
        locker.store(depositOf(alicesPhoneCase()));
        locker.release();

        Deposit bens = depositOf(bensPaperbacks());
        locker.store(bens);

        assertEquals(bens, locker.deposit().get());
    }

    @Test
    void lockerIsUnlockedOnlyByThePickupCodeOfItsParcel() {
        Locker locker = new Locker(1, LockerSize.SMALL);
        locker.store(depositOf(alicesPhoneCase()));

        assertTrue(locker.isUnlockedBy("482913"));
        assertFalse(locker.isUnlockedBy("000000"));
        assertFalse(locker.isUnlockedBy(null));
    }

    @Test
    void emptyLockerCannotBeUnlockedByAnyCode() {
        Locker emptyLocker = new Locker(1, LockerSize.SMALL);

        assertFalse(emptyLocker.isUnlockedBy("482913"));
    }

    @Test
    void lockerKnowsWhichParcelItHolds() {
        Locker locker = new Locker(1, LockerSize.SMALL);
        assertFalse(locker.holdsParcel("SPX-1001"));

        locker.store(depositOf(alicesPhoneCase()));

        assertTrue(locker.holdsParcel("SPX-1001"));
        assertFalse(locker.holdsParcel("SPX-1002"));
    }
}
