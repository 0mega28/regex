package com.example.regex.fsm;

import com.example.regex.util.CharacterSet;
import com.example.regex.util.Range;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Represent input string and the current position in String
 */
public class Cursor {
    /**
     * The entire input string
     */
    private final String string;
    private final int endIndex;
    /**
     * Captured groups
     */
    private final Map<Integer, Range<Integer>> group;
    private int startIndex;
    /**
     * The current index of the cursor
     */
    private int index;
    private OptionalInt previousMatchIndex;

    public Cursor(String string) {
        this.string = string;
        this.startIndex = 0;
        this.endIndex = string.length();
        this.group = new HashMap<>();
        this.index = 0;
        this.previousMatchIndex = OptionalInt.empty();
    }

    public Optional<Character> character() {
        return charAt(index);
    }

    public Optional<Character> charAt(int index) {
        return index < endIndex ? Optional.of(string.charAt(index)) : Optional.empty();
    }

    public Optional<Character> charOffsetBy(int offset) {
        return charAt(index + offset);
    }

    public Optional<String> substring(int offset) {
        return index + offset <= endIndex
                ? Optional.of(string.substring(index, index + offset))
                : Optional.empty();
    }

    public boolean isAtStart() {
        return index == startIndex;
    }

    public boolean isFromStart() {
        return startIndex == 0;
    }

    public boolean isAtLastIndex() {
        return index == endIndex - 1;
    }

    public boolean isAtWordBoundary() {
        if (character().isEmpty()) return true;
        char chr = character().get();
        char lhs = charOffsetBy(-1).orElse(' ');
        char rhs = charOffsetBy(1).orElse(' ');

        return isWord(chr)
                ? !isWord(lhs)
                : isWord(lhs) || !isWord(rhs);
    }

    public OptionalInt getPreviousMatchIndex() {
        return previousMatchIndex;
    }

    public int getIndex() {
        return index;
    }

    boolean isWord(char c) {
        return CharacterSet.word.contains(c);
    }
}
