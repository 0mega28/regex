package com.example.regex.fsm;

public sealed interface ConditionResult permits ConditionResult.rejected, ConditionResult.accepted {
    /**
     * A transition can be performed consuming `count` characters.
     * Epsilon transition consumes `0` character
     */
    record accepted(int count) implements ConditionResult {
    }

    /**
     * Transition cannot be performed
     */
    record rejected() implements ConditionResult {

    }
}
