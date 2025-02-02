package com.example.regex.util;

public record Range<BOUND extends Comparable<BOUND>>(BOUND lowerBound, BOUND upperBound) {
    public Range {
        if (lowerBound.compareTo(upperBound) > 0) {
            throw new IllegalArgumentException("lower bound must be greater than upper bound");
        }
    }

    public boolean contains(BOUND element) {
        return lowerBound.compareTo(element) <= 0 && upperBound.compareTo(element) >= 0;
    }

    public boolean isEmpty() {
        return lowerBound.compareTo(upperBound) == 0;
    }
}
