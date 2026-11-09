package com.smartlocker.result;

/** What the delivery agent gets back after storing a parcel. */
public record StoreReceipt(int lockerId, String pickupCode) {}
