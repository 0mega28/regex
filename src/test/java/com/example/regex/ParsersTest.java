package com.example.regex;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ParsersTest {

    @Test
    void string() {
        Parser<Void> abcdParser = Parsers.string("abcd");

        var successResult = abcdParser.parse("abcdef");
        assertEquals(new ParseResult<>(null, "ef"), successResult.orElseThrow());

        var failedResult = abcdParser.parse("abc");
        assertEquals(Optional.empty(), failedResult);
    }

    @Test
    void charParser() {
        Parser<Character> charParser = Parsers.charParser();

        var successResult = charParser.parse("abcd");
        assertEquals(new ParseResult<>('a', "bcd"), successResult.orElseThrow());

        var failedResult = charParser.parse("");
        assertEquals(Optional.empty(), failedResult);
    }

    @Test
    void numberParser() {
        var numParser = Parsers.number();

        assertEquals(1234, numParser.parse("1234abcd").orElseThrow().value());
        assertEquals("abcd", numParser.parse("1234abcd").orElseThrow().remaining());
    }

    @Test
    void end() {
        var endParser = Parsers.string("abcd")
            .flatMap(result -> Parsers.end().map(end -> result));

        assertEquals("", endParser.parse("abcd").orElseThrow().remaining());
        assertEquals(Optional.empty(), endParser.parse("abcdef"));
        assertEquals(Optional.empty(), endParser.parse("abc"));
    }
}