package com.smartlocker.service;

import com.smartlocker.codegenerator.RandomPickupCodeGenerator;
import com.smartlocker.exception.DuplicateParcelException;
import com.smartlocker.exception.InvalidInputException;
import com.smartlocker.exception.NoSuitableLockerException;
import com.smartlocker.exception.ParcelTooLargeException;
import com.smartlocker.feecalculator.TieredStorageFeeCalculator;
import com.smartlocker.model.LockerSize;
import com.smartlocker.result.LockerStatus;
import com.smartlocker.result.StoreReceipt;
import com.smartlocker.service.LockerService;

import static com.smartlocker.support.SampleParcels.alicesPhoneCase;
import static com.smartlocker.support.SampleParcels.bensPaperbacks;
import static com.smartlocker.support.SampleParcels.carolsEarbuds;
import static com.smartlocker.support.SampleParcels.davidsShoeBox;
import static com.smartlocker.support.SampleParcels.franksOfficeChair;
import static com.smartlocker.support.SampleStations.CLOCK;
import static com.smartlocker.support.SampleStations.FIRST_CODE;
import static com.smartlocker.support.SampleStations.SECOND_CODE;
import static com.smartlocker.support.SampleStations.station;
import static com.smartlocker.support.SampleStations.stationHandingOutCodes;
import static com.smartlocker.support.SampleStations.statusOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/** Level 1, part 2: storing a parcel in the smallest available locker. */
class StoreParcelTest {

    @Test
    void storingReturnsTheLockerIdAndPickupCode() {
        // Given one small locker
        LockerService station = stationHandingOutCodes(FIRST_CODE);
        station.createLocker(LockerSize.SMALL);

        // When the delivery agent stores Alice's phone case
        StoreReceipt receipt = station.storeParcel(alicesPhoneCase());

        // Then they are told the locker and the pickup code
        assertEquals(new StoreReceipt(1, FIRST_CODE), receipt);
    }

    @Test
    void listShowsWhichLockersAreOccupied() {
        // Given two small lockers
        LockerService station = station();
        station.createLocker(LockerSize.SMALL);
        station.createLocker(LockerSize.SMALL);

        // When Alice's phone case is stored
        station.storeParcel(alicesPhoneCase());

        // Then locker 1 shows as taken and locker 2 as free
        assertEquals(
                List.of(new LockerStatus(1, LockerSize.SMALL, false), new LockerStatus(2, LockerSize.SMALL, true)),
                station.listLockers());
    }

    @Test
    void phoneCaseGoesIntoTheSmallestLockerThatFits() {
        // Given a large, a medium and a small locker (created in that order)
        LockerService station = station();
        station.createLocker(LockerSize.LARGE); // locker 1
        station.createLocker(LockerSize.MEDIUM); // locker 2
        station.createLocker(LockerSize.SMALL); // locker 3

        // When Alice's phone case is stored
        StoreReceipt receipt = station.storeParcel(alicesPhoneCase());

        // Then it goes in the small locker, not the bigger ones
        assertEquals(3, receipt.lockerId());
    }

    @Test
    void parcelsMoveUpToBiggerLockersOnceSmallerOnesAreTaken() {
        // Given one locker of each size
        LockerService station = station();
        station.createLocker(LockerSize.SMALL); // locker 1
        station.createLocker(LockerSize.MEDIUM); // locker 2
        station.createLocker(LockerSize.LARGE); // locker 3

        // When three small parcels arrive one after another
        // Then they fill small, then medium, then large
        assertEquals(1, station.storeParcel(alicesPhoneCase()).lockerId());
        assertEquals(2, station.storeParcel(bensPaperbacks()).lockerId());
        assertEquals(3, station.storeParcel(carolsEarbuds()).lockerId());
    }

    @Test
    void shoeBoxSkipsSmallLockers() {
        // Given a small and a medium locker
        LockerService station = station();
        station.createLocker(LockerSize.SMALL); // locker 1
        station.createLocker(LockerSize.MEDIUM); // locker 2

        // When David's shoe box (needs MEDIUM) is stored
        // Then it goes in the medium locker even though the small one is free
        assertEquals(2, station.storeParcel(davidsShoeBox()).lockerId());
    }

    @Test
    void equalSizedLockersAreFilledInCreationOrder() {
        // Given two small lockers
        LockerService station = station();
        station.createLocker(LockerSize.SMALL); // locker 1
        station.createLocker(LockerSize.SMALL); // locker 2

        // When two small parcels arrive
        // Then the older locker is used first
        assertEquals(1, station.storeParcel(alicesPhoneCase()).lockerId());
        assertEquals(2, station.storeParcel(bensPaperbacks()).lockerId());
    }

    @Test
    void parcelIsRejectedWhenEverySuitableLockerIsFull() {
        // Given a single small locker that already holds Alice's phone case
        LockerService station = station();
        station.createLocker(LockerSize.SMALL);
        station.storeParcel(alicesPhoneCase());

        // When Ben's paperbacks arrive
        // Then there is nowhere to put them
        assertThrows(NoSuitableLockerException.class, () -> station.storeParcel(bensPaperbacks()));
    }

    @Test
    void shoeBoxIsRejectedWhenOnlySmallLockersExist() {
        // Given only a small locker
        LockerService station = station();
        station.createLocker(LockerSize.SMALL);

        // When David's shoe box arrives
        NoSuitableLockerException error =
                assertThrows(NoSuitableLockerException.class, () -> station.storeParcel(davidsShoeBox()));

        // Then the message says what size was needed
        assertTrue(error.getMessage().contains("MEDIUM"));
    }

    @Test
    void parcelIsRejectedWhenThereAreNoLockersAtAll() {
        assertThrows(NoSuitableLockerException.class, () -> station().storeParcel(alicesPhoneCase()));
    }

    @Test
    void officeChairIsTooLargeEvenWhenLockersAreFree() {
        // Given a free large locker
        LockerService station = station();
        station.createLocker(LockerSize.LARGE);

        // When Frank's office chair arrives
        // Then it gets its own "too large" error, not "no locker available"
        assertThrows(ParcelTooLargeException.class, () -> station.storeParcel(franksOfficeChair()));
    }

    @Test
    void failedStoreDoesNotUseUpAPickupCode() {
        // Given a small locker only, and a shoe box that cannot be stored
        LockerService station = stationHandingOutCodes(FIRST_CODE);
        station.createLocker(LockerSize.SMALL);
        assertThrows(NoSuitableLockerException.class, () -> station.storeParcel(davidsShoeBox()));

        // When a medium locker is added and the shoe box is stored
        station.createLocker(LockerSize.MEDIUM);
        StoreReceipt receipt = station.storeParcel(davidsShoeBox());

        // Then it still gets the first code (the failed attempt did not consume one)
        assertEquals(FIRST_CODE, receipt.pickupCode());
    }

    @Test
    void sameParcelCannotBeStoredTwice() {
        // Given Alice's phone case is already in locker 1
        LockerService station = station();
        station.createLocker(LockerSize.SMALL);
        station.createLocker(LockerSize.SMALL);
        station.storeParcel(alicesPhoneCase());

        // When the same parcel (same tracking ID) is scanned in again
        // Then it is rejected and locker 2 stays free
        assertThrows(DuplicateParcelException.class, () -> station.storeParcel(alicesPhoneCase()));
        assertTrue(statusOf(station, 2).available());
    }

    @Test
    void duplicateIsReportedEvenWhenAllLockersAreFull() {
        LockerService station = station();
        station.createLocker(LockerSize.SMALL);
        station.storeParcel(alicesPhoneCase());

        assertThrows(DuplicateParcelException.class, () -> station.storeParcel(alicesPhoneCase()));
    }

    @Test
    void parcelIsRequired() {
        assertThrows(InvalidInputException.class, () -> station().storeParcel(null));
    }

    @Test
    void failsSafelyWhenNoUniquePickupCodeCanBeGenerated() {
        AtomicInteger attempts = new AtomicInteger();
        RandomPickupCodeGenerator repeatingGenerator = new RandomPickupCodeGenerator() {
            @Override
            public String next() {
                attempts.incrementAndGet();
                return FIRST_CODE;
            }
        };
        LockerService station = new LockerService(
                repeatingGenerator, CLOCK, new TieredStorageFeeCalculator(BigDecimal.ONE));
        station.createLocker(LockerSize.SMALL);
        station.createLocker(LockerSize.SMALL);
        station.storeParcel(alicesPhoneCase());

        assertThrows(IllegalStateException.class, () -> station.storeParcel(bensPaperbacks()));

        assertEquals(101, attempts.get());
        assertTrue(statusOf(station, 2).available());
    }

    @Test
    void parcelsStoredAtTheSameTimeNeverShareAPickupCode() {
        // Given a code generator that repeats itself: 482913, 482913, 715204
        LockerService station = stationHandingOutCodes(FIRST_CODE, FIRST_CODE, SECOND_CODE);
        station.createLocker(LockerSize.SMALL);
        station.createLocker(LockerSize.SMALL);

        // When two parcels are stored
        String alicesCode = station.storeParcel(alicesPhoneCase()).pickupCode();
        String bensCode = station.storeParcel(bensPaperbacks()).pickupCode();

        // Then the repeated code is skipped for the second parcel
        assertEquals(FIRST_CODE, alicesCode);
        assertEquals(SECOND_CODE, bensCode);
    }
}
