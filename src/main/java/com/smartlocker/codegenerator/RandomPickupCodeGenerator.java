
package com.smartlocker.codegenerator;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Generates six-digit pickup codes.
 * Codes that are easy to guess, such as 111111, 123456 or 500000, are skipped.
 */
public class RandomPickupCodeGenerator {

    private static final int CODE_LENGTH = 6;
    private static final int NUMBER_OF_CODES = 1_000_000;
    private static final int MAX_GENERATION_ATTEMPTS = 100;

    private final Random random;

    public RandomPickupCodeGenerator() {
        this(new SecureRandom());
    }

    RandomPickupCodeGenerator(Random random) {
        this.random = random;
    }

    public String next() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String code = randomCode();

            if (!isEasyToGuess(code)) {
                return code;
            }
        }

        throw new IllegalStateException(
                "Unable to generate an acceptable pickup code after "
                        + MAX_GENERATION_ATTEMPTS + " attempts");
    }

    private String randomCode() {
        return String.format("%06d", random.nextInt(NUMBER_OF_CODES));
    }

    static boolean isEasyToGuess(String code) {
        return isOneDigitRepeated(code)
                || isStraightRun(code)
                || isRoundNumber(code);
    }

    /** Codes like 111111 or 999999. */
    private static boolean isOneDigitRepeated(String code) {
        return code.chars().distinct().count() == 1;
    }

    /** Codes like 123456 or 654321. */
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

    /** Codes like 100000 or 500000. */
    private static boolean isRoundNumber(String code) {
        return code.endsWith("00000");
    }
}
