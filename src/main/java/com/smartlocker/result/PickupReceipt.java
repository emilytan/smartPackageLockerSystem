package com.smartlocker.result;

/** Confirmation that a parcel was collected. */
public record PickupReceipt(String trackingId, int lockerId) {}
