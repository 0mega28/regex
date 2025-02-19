package com.example.regex.fsm;

import com.example.regex.fsm.ConditionResult.accepted;
import com.example.regex.fsm.ConditionResult.rejected;
import com.example.regex.util.CharacterSet;

import java.util.function.Function;

import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;

@FunctionalInterface
public interface Condition extends Function<Cursor, ConditionResult> {
    default ConditionResult canPerformTransition(Cursor cursor) {
        return this.apply(cursor);
    }
}

record MatchCharacter(char character, boolean ignoreCase) implements Condition {

    @Override
    public ConditionResult apply(Cursor cursor) {
        return cursor.character()
                .filter(this::matches)
                .map(_ignore -> (ConditionResult) new accepted())
                .orElse(new rejected());
    }

    private boolean matches(Character inputChr) {
        return ignoreCase
                ? toLowerCase(inputChr) == toLowerCase(character)
                : inputChr == character;
    }
}

record MatchString(String string, int count, boolean ignoreCase) implements Condition {

    @Override
    public ConditionResult apply(Cursor cursor) {
        return cursor.substring(count)
                .filter(this::matches)
                .map(_ignore -> (ConditionResult) new accepted(count))
                .orElse(new rejected());
    }

    private boolean matches(String inputStr) {
        return ignoreCase
                ? inputStr.equalsIgnoreCase(string)
                : inputStr.equals(string);
    }
}

record MatchCharacterSet(CharacterSet set, boolean ignoreCase, boolean isNegative) implements Condition {
    @Override
    public ConditionResult apply(Cursor cursor) {
        return cursor.character()
                .filter(this::matches)
                .map(_ignore -> (ConditionResult) new accepted())
                .orElse(new rejected());
    }

    private boolean matches(Character inputChr) {
        return isNegative != ignoreCase
                ? set.contains(toLowerCase(inputChr)) || set.contains(toUpperCase(inputChr))
                : set.contains(inputChr);
    }
}

record MatchAnyCharacter(boolean includingNewLine) implements Condition {
    @Override
    public ConditionResult apply(Cursor cursor) {
        return cursor.character()
                .filter(ch -> includingNewLine || ch != '\n')
                .map(_ignore -> (ConditionResult) new accepted())
                .orElse(new rejected());
    }
}

record BackReference(int groupIndex) implements Condition {

    @Override
    public ConditionResult apply(Cursor cursor) {
        throw new UnsupportedOperationException();
    }
}