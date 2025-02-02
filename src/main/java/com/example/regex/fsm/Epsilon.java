package com.example.regex.fsm;

import com.example.regex.fsm.ConditionResult.accepted;
import com.example.regex.fsm.ConditionResult.rejected;

import java.util.function.Predicate;

public record Epsilon(Predicate<Cursor> predicate) implements Condition {

    public Epsilon() {
        this(null);
    }

    @Override
    public ConditionResult apply(Cursor cursor) {
        if (predicate != null)
            return predicate.test(cursor) ? new accepted(0) : new rejected();
        return new accepted(0);
    }
}
