package com.smartlocker.service;

import static com.smartlocker.support.SampleParcels.alicesPhoneCase;
import static com.smartlocker.support.SampleParcels.bensPaperbacks;
import static com.smartlocker.support.SampleStations.FIRST_CODE;
import static com.smartlocker.support.SampleStations.SECOND_CODE;
import static com.smartlocker.support.SampleStations.WRONG_CODE;
import static com.smartlocker.support.SampleStations.station;
import static com.smartlocker.support.SampleStations.stationHandingOutCodes;
import static com.smartlocker.support.SampleStations.statusOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.smartlocker.exception.InvalidPickupException;
import com.smartlocker.model.LockerSize;
import com.smartlocker.result.PickupReceipt;
import com.smartlocker.result.StoreReceipt;
import org.junit.jupiter.api.Test;

/** Level 2: collecting a parcel with the locker ID and pickup code, including everything that can go wrong. */
class CollectParcelTest {

    @Test
    void validPickupConfirmsTheParcelAndLocker() {
        // Given Alice's phone case is in locker 1 with code 482913
        LockerService station = stationHandingOutCodes(FIRST_CODE);
        station.createLocker(LockerSize.SMALL);
        station.storeParcel(alicesPhoneCase());

        // When she enters locker 1 and her code
        PickupReceipt receipt = station.retrieveParcel(1, FIRST_CODE);

        // Then the pickup is confirmed for her parcel
        assertEquals(new PickupReceipt("SPX-1001", 1, java.math.BigDecimal.ZERO), receipt);
    }

    @Test
    void lockerIsAvailableAgainAfterPickup() {
        LockerService station = stationHandingOutCodes(FIRST_CODE);
        station.createLocker(LockerSize.SMALL);
        station.storeParcel(alicesPhoneCase());

        station.retrieveParcel(1, FIRST_CODE);

        assertTrue(statusOf(station, 1).available());
    }

    @Test
    void lockerCanBeReusedAfterPickup() {
        // Given Alice collected her phone case from locker 1
        LockerService station = stationHandingOutCodes(FIRST_CODE, SECOND_CODE);
        station.createLocker(LockerSize.SMALL);
        station.storeParcel(alicesPhoneCase());
        station.retrieveParcel(1, FIRST_CODE);

        // When Ben's paperbacks are stored
        StoreReceipt receipt = station.storeParcel(bensPaperbacks());

        // Then they go into the same locker
        assertEquals(new StoreReceipt(1, SECOND_CODE), receipt);
    }

    @Test
    void parcelCanBeStoredAgainOnceItWasCollected() {
        // Given Alice collected her phone case
        LockerService station = stationHandingOutCodes(FIRST_CODE, SECOND_CODE);
        station.createLocker(LockerSize.SMALL);
        station.storeParcel(alicesPhoneCase());
        station.retrieveParcel(1, FIRST_CODE);

        // When the same tracking ID arrives again (for example a return shipment)
        // Then it is accepted
        assertEquals(1, station.storeParcel(alicesPhoneCase()).lockerId());
    }

    @Test
    void wrongCodeIsRejectedAndTheParcelStaysInTheLocker() {
        // Given Alice's phone case is in locker 1
        LockerService station = stationHandingOutCodes(FIRST_CODE);
        station.createLocker(LockerSize.SMALL);
        station.storeParcel(alicesPhoneCase());

        // When someone enters the wrong code
        // Then it is rejected and the parcel is still there
        assertThrows(InvalidPickupException.class, () -> station.retrieveParcel(1, WRONG_CODE));
        assertFalse(statusOf(station, 1).available());

        // And Alice can still collect it with the right code
        assertEquals(new PickupReceipt("SPX-1001", 1, java.math.BigDecimal.ZERO), station.retrieveParcel(1, FIRST_CODE));
    }

    @Test
    void unknownLockerIsRejected() {
        assertThrows(InvalidPickupException.class, () -> station().retrieveParcel(99, FIRST_CODE));
    }

    @Test
    void emptyLockerIsRejected() {
        LockerService station = station();
        station.createLocker(LockerSize.SMALL);

        assertThrows(InvalidPickupException.class, () -> station.retrieveParcel(1, FIRST_CODE));
    }

    @Test
    void codeCannotBeUsedTwice() {
        // Given Alice already collected her parcel with her code
        LockerService station = stationHandingOutCodes(FIRST_CODE);
        station.createLocker(LockerSize.SMALL);
        station.storeParcel(alicesPhoneCase());
        station.retrieveParcel(1, FIRST_CODE);

        // When the same code is entered again
        // Then it is rejected
        assertThrows(InvalidPickupException.class, () -> station.retrieveParcel(1, FIRST_CODE));
    }

    @Test
    void codeFromOneLockerDoesNotOpenAnother() {
        // Given Alice's parcel is in locker 1 and Ben's is in locker 2
        LockerService station = stationHandingOutCodes(FIRST_CODE, SECOND_CODE);
        station.createLocker(LockerSize.SMALL);
        station.createLocker(LockerSize.SMALL);
        station.storeParcel(alicesPhoneCase());
        station.storeParcel(bensPaperbacks());

        // When Alice's code is entered at Ben's locker
        // Then it is rejected
        assertThrows(InvalidPickupException.class, () -> station.retrieveParcel(2, FIRST_CODE));
    }

    @Test
    void missingCodeIsRejected() {
        LockerService station = stationHandingOutCodes(FIRST_CODE);
        station.createLocker(LockerSize.SMALL);
        station.storeParcel(alicesPhoneCase());

        assertThrows(InvalidPickupException.class, () -> station.retrieveParcel(1, null));
    }

    @Test
    void everyPickupFailureLooksTheSame() {
        // Given locker 1 holds a parcel and locker 2 is empty
        LockerService station = stationHandingOutCodes(FIRST_CODE);
        station.createLocker(LockerSize.SMALL);
        station.createLocker(LockerSize.SMALL);
        station.storeParcel(alicesPhoneCase());

        // When a wrong code, an unknown locker and an empty locker are each tried
        String wrongCode = assertThrows(InvalidPickupException.class, () -> station.retrieveParcel(1, WRONG_CODE)).getMessage();
        String unknownLocker = assertThrows(InvalidPickupException.class, () -> station.retrieveParcel(99, FIRST_CODE)).getMessage();
        String emptyLocker = assertThrows(InvalidPickupException.class, () -> station.retrieveParcel(2, FIRST_CODE)).getMessage();

        // Then the message is identical, so it reveals nothing to someone guessing
        assertEquals(wrongCode, unknownLocker);
        assertEquals(wrongCode, emptyLocker);
    }

    @Test
    void codeCanBeHandedOutAgainOnceItsParcelWasCollected() {
        // Given the generator repeats a code that Alice already used and collected
        LockerService station = stationHandingOutCodes(FIRST_CODE, FIRST_CODE);
        station.createLocker(LockerSize.SMALL);
        station.storeParcel(alicesPhoneCase());
        station.retrieveParcel(1, FIRST_CODE);

        // When Ben's paperbacks are stored
        StoreReceipt receipt = station.storeParcel(bensPaperbacks());

        // Then the same code can be used again, because it no longer belongs to a stored parcel
        assertEquals(FIRST_CODE, receipt.pickupCode());
    }
}
