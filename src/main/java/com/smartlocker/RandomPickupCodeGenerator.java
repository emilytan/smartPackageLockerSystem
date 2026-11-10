package com.smartlocker;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Generates random six-digit codes that are hard to guess. A code never starts with 0 (so no 000001),
 * and codes like 111111, 123456 or 500000 are skipped.
 */
public class RandomPickupCodeGenerator {

    static final int LOWEST_CODE = 100_000; // the lowest six-digit number that does not start with 0
    private static final int NUMBER_OF_CODES = 900_000; // 100000 up to 999999

    private final Random random;

    public RandomPickupCodeGenerator() {
        this(new SecureRandom());
    }

    RandomPickupCodeGenerator(Random random) {
        this.random = random;
    }

    public String next() {
        String code = randomCode();
        while (isEasyToGuess(code)) {
            code = randomCode();
        }
        return code;
    }

    private String randomCode() {
        return String.valueOf(LOWEST_CODE + random.nextInt(NUMBER_OF_CODES));
    }

    static boolean isEasyToGuess(String code) {
        return isOneDigitRepeated(code) || isStraightRun(code) || isRoundNumber(code);
    }

    /** Codes like 111111 or 999999. */
    private static boolean isOneDigitRepeated(String code) {
        return code.chars().distinct().count() == 1;
    }

    /** Codes like 123456 or 654321, where every digit is one more (or one less) than the digit before it. */
    private static boolean isStraightRun(String code) {
        boolean countingUp = true;
        boolean countingDown = true;
        for (int i = 1; i < code.length(); i++) {
            int step = code.charAt(i) - code.charAt(i - 1);
            if (step != 1) {
                countingUp = false;
            }
            if (step != -1) {
                countingDown = false;
            }
        }
        return countingUp || countingDown;
    }

    /** Codes like 100000 or 500000: one digit followed by zeros. */
    private static boolean isRoundNumber(String code) {
        return code.endsWith("00000");
    }
}
