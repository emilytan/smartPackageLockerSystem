package com.smartlocker.model;

import com.smartlocker.exception.ParcelTooLargeException;

import java.util.Arrays;

/**
 * The three sizes of locker compartment. Declaration order matters: smallest first.
 * Each size is described by the largest parcel it can hold, in cm, longest side first.
 */
public enum LockerSize {
    SMALL(40, 30, 10),
    MEDIUM(40, 30, 20),
    LARGE(40, 30, 30);

    private final int maxLongestCm;
    private final int maxMiddleCm;
    private final int maxShortestCm;

    LockerSize(int maxLongestCm, int maxMiddleCm, int maxShortestCm) {
        this.maxLongestCm = maxLongestCm;
        this.maxMiddleCm = maxMiddleCm;
        this.maxShortestCm = maxShortestCm;
    }

    /** True if a locker of this size can hold a parcel that needs the given size (same size or bigger). */
    public boolean isBigEnoughFor(LockerSize sizeNeeded) {
        return compareTo(sizeNeeded) >= 0;
    }

    /** The smallest locker size that can hold a parcel with these dimensions (parcel may be turned any way). */
    public static LockerSize smallestFor(int lengthCm, int widthCm, int heightCm) {
        for (LockerSize size : values()) {
            if (size.canHold(lengthCm, widthCm, heightCm)) {
                return size;
            }
        }
        throw new ParcelTooLargeException(
                "No locker is large enough for a parcel of " + lengthCm + " x " + widthCm + " x " + heightCm + " cm");
    }

    private boolean canHold(int lengthCm, int widthCm, int heightCm) {
        int[] sides = {lengthCm, widthCm, heightCm};
        Arrays.sort(sides); // shortest first
        int shortest = sides[0];
        int middle = sides[1];
        int longest = sides[2];
        return longest <= maxLongestCm && middle <= maxMiddleCm && shortest <= maxShortestCm;
    }
}
