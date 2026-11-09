package com.smartlocker.service;

import com.smartlocker.contract.PickupCodeGenerator;
import com.smartlocker.exception.InvalidInputException;
import com.smartlocker.model.Locker;
import com.smartlocker.model.LockerSize;
import com.smartlocker.result.LockerStatus;
import java.time.Clock;
import java.util.ArrayList;
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
}
