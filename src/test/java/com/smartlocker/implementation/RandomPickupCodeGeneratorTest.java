package com.smartlocker.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RandomPickupCodeGeneratorTest {

    /** A "random" source that returns exactly these numbers, in order, so a test controls what is generated. */
    private static Random returning(int... numbers) {
        PrimitiveIterator.OfInt remaining = Arrays.stream(numbers).iterator();
        return new Random() {
            @Override
            public int nextInt(int bound) {
                return remaining.nextInt();
            }
        };
    }

    private static boolean isSixDigitNumber(String code) {
        return code.length() == 6 && code.chars().allMatch(Character::isDigit);
    }

    // ---- What a generated code looks like ----

    @Test
    void codeIsAlwaysASixDigitNumber() {
        RandomPickupCodeGenerator generator = new RandomPickupCodeGenerator();

        for (int i = 0; i < 1_000; i++) {
            assertTrue(isSixDigitNumber(generator.next()));
        }
    }

    @Test
    void codeNeverStartsWithZero() {
        RandomPickupCodeGenerator generator = new RandomPickupCodeGenerator();

        for (int i = 0; i < 1_000; i++) {
            assertFalse(generator.next().startsWith("0"));
        }
    }

    @Test
    void codesVary() {
        RandomPickupCodeGenerator generator = new RandomPickupCodeGenerator();
        Set<String> seen = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            seen.add(generator.next());
        }

        assertTrue(seen.size() > 1);
    }

    @Test
    void generatedCodesAreNeverEasyToGuess() {
        RandomPickupCodeGenerator generator = new RandomPickupCodeGenerator();

        for (int i = 0; i < 1_000; i++) {
            assertFalse(RandomPickupCodeGenerator.isEasyToGuess(generator.next()));
        }
    }

    // ---- Which codes count as easy to guess? ----

    @Test
    void sameDigitRepeatedIsEasyToGuess() {
        assertTrue(RandomPickupCodeGenerator.isEasyToGuess("111111"));
        assertTrue(RandomPickupCodeGenerator.isEasyToGuess("999999"));
    }

    @Test
    void roundNumbersMadeOfOneDigitAndZerosAreEasyToGuess() {
        assertTrue(RandomPickupCodeGenerator.isEasyToGuess("100000"));
        assertTrue(RandomPickupCodeGenerator.isEasyToGuess("500000"));
        assertTrue(RandomPickupCodeGenerator.isEasyToGuess("900000"));
    }

    @Test
    void straightRunsOfDigitsAreEasyToGuess() {
        assertTrue(RandomPickupCodeGenerator.isEasyToGuess("123456"));
        assertTrue(RandomPickupCodeGenerator.isEasyToGuess("234567"));
        assertTrue(RandomPickupCodeGenerator.isEasyToGuess("654321"));
        assertTrue(RandomPickupCodeGenerator.isEasyToGuess("876543"));
    }

    @Test
    void ordinaryLookingCodesAreAccepted() {
        assertFalse(RandomPickupCodeGenerator.isEasyToGuess("482913"));
        assertFalse(RandomPickupCodeGenerator.isEasyToGuess("715204"));
        assertFalse(RandomPickupCodeGenerator.isEasyToGuess("390127"));
    }

    // ---- Easy codes are thrown away and replaced ----

    @Test
    void easyToGuessCandidatesAreSkipped() {
        // The random source first suggests 100000, 111111 and 123456 (all easy), then 482913.
        Random source = returning(0, 11_111, 23_456, 382_913);

        String code = new RandomPickupCodeGenerator(source).next();

        assertEquals("482913", code);
    }
}
