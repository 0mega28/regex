package com.example.regex.ast;

public enum Anchor implements Unit {
    START_OF_STRING,
    END_OF_STRING,
    WORD_BOUNDARY,
    NON_WORD_BOUNDARY,
    START_OF_STRING_ONLY,
    END_OF_STRING_ONLY,
    END_OF_STRING_ONLY_NOT_NEWLINE,
    PREVIOUS_MATCH_END;

    @Override
    public String toString() {
        return "Anchor." + name().toLowerCase();
    }
}
