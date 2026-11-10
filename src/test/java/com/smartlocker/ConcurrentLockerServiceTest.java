package com.smartlocker;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

class ConcurrentLockerServiceTest {

    @Test
    void assignsEachLockerToAtMostOneOfTwentyConcurrentRequests() throws Exception {
        int lockerCount = 10;
        int requestCount = 20;
        var codeSequence = new java.util.concurrent.atomic.AtomicInteger();
        LockerService service = new LockerService(new RandomPickupCodeGenerator() {
            @Override
            public String next() {
                return String.valueOf(100_000 + codeSequence.incrementAndGet());
            }
        }, Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC),
                new TieredStorageFeeCalculator(BigDecimal.ONE));

        for (int i = 0; i < lockerCount; i++) {
            service.createLocker(LockerSize.SMALL);
        }

        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(requestCount)) {
            List<Future<Integer>> results = new ArrayList<>();
            for (int i = 0; i < requestCount; i++) {
                final int parcelNumber = i;
                Callable<Integer> request = () -> {
                    ready.countDown();
                    start.await();
                    try {
                        return service.storeParcel(new Parcel(
                                "TRACK-" + parcelNumber, "Customer", 10, 10, 10)).lockerId();
                    } catch (com.smartlocker.NoSuitableLockerException unavailable) {
                        return -1;
                    }
                };
                results.add(executor.submit(request));
            }
            ready.await();
            start.countDown();

            List<Integer> assignedLockerIds = new ArrayList<>();
            int rejected = 0;
            for (Future<Integer> result : results) {
                int lockerId = result.get();
                if (lockerId == -1) {
                    rejected++;
                } else {
                    assignedLockerIds.add(lockerId);
                }
            }

            assertEquals(lockerCount, assignedLockerIds.size());
            assertEquals(requestCount - lockerCount, rejected);
            assertEquals(lockerCount, assignedLockerIds.stream().distinct().count());
            assertNotEquals(-1, assignedLockerIds.get(0));
            assertEquals(lockerCount, service.listLockers().stream().filter(s -> !s.available()).count());
        }
    }
}
