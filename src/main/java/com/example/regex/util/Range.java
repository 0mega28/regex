package com.example.regex.util;

/**
 * Represents a range with a lower (inclusive) and upper bound (exclusive).
 *
 * @param <BOUND> the type of the bounds, must be comparable.
 */
public record Range<BOUND extends Comparable<BOUND>>(BOUND lowerBound, BOUND upperBound) {
    /**
     * Constructs a new Range.
     *
     * @param lowerBound the lower bound of the range.
     * @param upperBound the upper bound of the range exclusive.
     * @throws IllegalArgumentException if the lower bound is greater than the upper bound.
     */
    public Range {
        if (lowerBound.compareTo(upperBound) > 0) {
            throw new IllegalArgumentException("lower bound must be less than or equal to upper bound");
        }
    }

    /**
     * Checks if the given element is contained within this range.
     *
     * @param element the element to check.
     * @return true if the element is within the range (inclusive of lower bound, exclusive of upper bound), false otherwise.
     */
    public boolean contains(BOUND element) {
        return lowerBound.compareTo(element) <= 0 && upperBound.compareTo(element) > 0;
    }

    public boolean isEmpty() {
        return lowerBound.compareTo(upperBound) == 0;
    }
}
