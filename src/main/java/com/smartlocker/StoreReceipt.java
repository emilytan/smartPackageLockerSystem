package com.smartlocker;

/** What the delivery agent gets back after storing a parcel. */
public record StoreReceipt(int lockerId, String pickupCode) {}
