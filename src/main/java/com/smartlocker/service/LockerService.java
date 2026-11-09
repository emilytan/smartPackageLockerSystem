package com.smartlocker.service;

import com.smartlocker.contract.PickupCodeGenerator;
import com.smartlocker.exception.DuplicateParcelException;
import com.smartlocker.exception.InvalidInputException;
import com.smartlocker.exception.NoSuitableLockerException;
import com.smartlocker.model.Deposit;
import com.smartlocker.model.Locker;
import com.smartlocker.model.LockerSize;
import com.smartlocker.model.Parcel;
import com.smartlocker.result.LockerStatus;
import com.smartlocker.result.StoreReceipt;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The use cases of a locker station: create lockers, list them, store a parcel and collect a parcel.
 *
 * <p>Not thread-safe yet. Safe concurrent access is added in the next step, test-first.
 */
public class LockerService {

    private final List<Locker> lockers = new ArrayList<>(); // kept in creation order
    private final PickupCodeGenerator codeGenerator;
    private final Clock clock;

    public LockerService(PickupCodeGenerator codeGenerator, Clock clock) {
        this.codeGenerator = codeGenerator;
        this.clock = clock;
    }

    public LockerStatus createLocker(LockerSize size) {
        if (size == null) {
            throw new InvalidInputException("Locker size is required");
        }
        Locker locker = new Locker(lockers.size() + 1, size);
        lockers.add(locker);
        return LockerStatus.of(locker);
    }

    public List<LockerStatus> listLockers() {
        return lockers.stream().map(LockerStatus::of).toList();
    }

    public StoreReceipt storeParcel(Parcel parcel) {
        if (parcel == null) {
            throw new InvalidInputException("Parcel is required");
        }
        LockerSize sizeNeeded = parcel.lockerSizeNeeded();
        rejectIfAlreadyStored(parcel);
        Locker locker = findSmallestAvailableLocker(sizeNeeded);

        String pickupCode = generateUnusedPickupCode();
        locker.store(new Deposit(parcel, pickupCode, clock.instant()));
        return new StoreReceipt(locker.id(), pickupCode);
    }

    private void rejectIfAlreadyStored(Parcel parcel) {
        boolean alreadyStored = lockers.stream().anyMatch(locker -> locker.holdsParcel(parcel.trackingId()));
        if (alreadyStored) {
            throw new DuplicateParcelException(parcel.trackingId());
        }
    }

    private Locker findSmallestAvailableLocker(LockerSize sizeNeeded) {
        return lockers.stream()
                .filter(Locker::isAvailable) // only free lockers
                .filter(locker -> locker.size().isBigEnoughFor(sizeNeeded)) // only lockers big enough for the parcel
                .min(Comparator.comparing(Locker::size)) // the smallest of those; on a tie the oldest locker wins
                .orElseThrow(() -> new NoSuitableLockerException(
                        "No available locker can hold a parcel that needs a " + sizeNeeded + " locker or larger"));
    }

    private String generateUnusedPickupCode() {
        String code = codeGenerator.next();
        while (isPickupCodeInUse(code)) {
            code = codeGenerator.next();
        }
        return code;
    }

    private boolean isPickupCodeInUse(String pickupCode) {
        return lockers.stream().anyMatch(locker -> locker.isUnlockedBy(pickupCode));
    }
}
