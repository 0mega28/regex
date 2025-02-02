package com.example.regex.fsm;

import java.util.function.Function;

@FunctionalInterface
public interface Condition extends Function<Cursor, ConditionResult> {
    default ConditionResult canPerformTransition(Cursor cursor) {
        return this.apply(cursor);
    }
}

