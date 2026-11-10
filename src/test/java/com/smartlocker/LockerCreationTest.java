package com.smartlocker;


import static com.smartlocker.support.SampleStations.station;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Level 1, part 1: creating lockers and listing them. */
class LockerCreationTest {

    @Test
    void newStationHasNoLockers() {
        assertTrue(station().listLockers().isEmpty());
    }

    @Test
    void createdLockersGetSequentialIdsAndStartAvailable() {
        LockerService station = station();

        LockerStatus first = station.createLocker(LockerSize.SMALL);
        LockerStatus second = station.createLocker(LockerSize.LARGE);

        assertEquals(new LockerStatus(1, LockerSize.SMALL, true), first);
        assertEquals(new LockerStatus(2, LockerSize.LARGE, true), second);
        assertEquals(List.of(first, second), station.listLockers());
    }

    @Test
    void lockerSizeIsRequired() {
        assertThrows(InvalidInputException.class, () -> station().createLocker(null));
    }
}
