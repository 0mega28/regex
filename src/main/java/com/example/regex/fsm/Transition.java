package com.example.regex.fsm;

import java.util.function.Predicate;

public record Transition(State end, Condition condition) {

    /**
     * Creates a Transition which doesn't consume characters
     */
    static Transition epsilon(State end, Predicate<Cursor> condition) {
        return new Transition(end, new Epsilon(condition));
    }

    /**
     * Creates a unconditional Transition which doesn't consume characters.
     */
    static Transition epsilon(State end) {
        return new Transition(end, new Epsilon());
    }
}
