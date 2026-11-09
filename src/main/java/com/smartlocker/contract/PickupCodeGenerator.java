package com.smartlocker.contract;

/**
 * Something that makes pickup codes. The service only needs to ask for the next code; how the code
 * is made is up to the implementation. Keeping codes unique among stored parcels is the service's job.
 */
@FunctionalInterface
public interface PickupCodeGenerator {

    String next();
}
