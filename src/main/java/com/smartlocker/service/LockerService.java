package com.smartlocker.service;

import com.smartlocker.contract.PickupCodeGenerator;
import com.smartlocker.contract.StorageFeeCalculator;
import com.smartlocker.exception.DuplicateParcelException;
import com.smartlocker.exception.InvalidInputException;
import com.smartlocker.exception.InvalidPickupException;
import com.smartlocker.exception.NoSuitableLockerException;
import com.smartlocker.model.Deposit;
import com.smartlocker.model.Locker;
import com.smartlocker.model.LockerSize;
import com.smartlocker.model.Parcel;
import com.smartlocker.result.LockerStatus;
import com.smartlocker.result.PickupReceipt;
import com.smartlocker.result.StoreReceipt;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

/**
 * The use cases of a locker station: create lockers, list them, store a parcel and collect a parcel.
 *
 * <p>All public operations are synchronized on this service instance. This makes each
 * in-memory use case atomic and prevents two requests from allocating the same locker.
 */
public class LockerService {

    private final List<Locker> lockers = new ArrayList<>(); // kept in creation order
    private final PickupCodeGenerator codeGenerator;
    private static final int MAX_PICKUP_CODE_ATTEMPTS = 100;

    private final Clock clock;
    private final StorageFeeCalculator storageFeeCalculator;

    public LockerService(
            PickupCodeGenerator codeGenerator,
            Clock clock,
            StorageFeeCalculator storageFeeCalculator) {
        this.codeGenerator = Objects.requireNonNull(codeGenerator, "codeGenerator");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.storageFeeCalculator = Objects.requireNonNull(storageFeeCalculator, "storageFeeCalculator");
    }

    public synchronized LockerStatus createLocker(LockerSize size) {
        if (size == null) {
            throw new InvalidInputException("Locker size is required");
        }
        Locker locker = new Locker(lockers.size() + 1, size);
        lockers.add(locker);
        return LockerStatus.of(locker);
    }

    public synchronized List<LockerStatus> listLockers() {
        return lockers.stream().map(LockerStatus::of).toList();
    }

    public synchronized StoreReceipt storeParcel(Parcel parcel) {
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

    public synchronized PickupReceipt retrieveParcel(int lockerId, String pickupCode) {
        Locker locker = findLocker(lockerId).orElseThrow(InvalidPickupException::new);
        if (!locker.isUnlockedBy(pickupCode)) { // covers both a wrong code and an empty locker
            throw new InvalidPickupException();
        }

        Deposit deposit = locker.deposit().orElseThrow(InvalidPickupException::new);
        // Calculate before releasing the parcel so a calculation failure leaves the locker intact.
        var storageFee = storageFeeCalculator.calculate(deposit.storedAt(), clock.instant());
        locker.release();
        return new PickupReceipt(deposit.parcel().trackingId(), locker.id(), storageFee);
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
        for (int attempt = 0; attempt < MAX_PICKUP_CODE_ATTEMPTS; attempt++) {
            String code = codeGenerator.next();
            if (code == null || code.isBlank()) {
                throw new IllegalStateException("Pickup code generator returned an empty code");
            }
            if (!isPickupCodeInUse(code)) {
                return code;
            }
        }
        throw new IllegalStateException(
                "Unable to generate a unique pickup code after " + MAX_PICKUP_CODE_ATTEMPTS + " attempts");
    }

    private boolean isPickupCodeInUse(String pickupCode) {
        return lockers.stream().anyMatch(locker -> locker.isUnlockedBy(pickupCode));
    }

    private Optional<Locker> findLocker(int lockerId) {
        return lockers.stream().filter(locker -> locker.id() == lockerId).findFirst();
    }
}
