package com.example.regex.fsm;

public sealed interface ConditionResult {
    /**
     * A transition can be performed consuming `count` characters.
     * Epsilon transition consumes `0` character
     */
    record accepted(int count) implements ConditionResult {
        public accepted() {
            this(1);
        }
    }

    /**
     * Transition cannot be performed
     */
    record rejected() implements ConditionResult {

    }
}
