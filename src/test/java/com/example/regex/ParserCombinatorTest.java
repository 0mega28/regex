package com.example.regex;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ParserCombinatorTest {

    @Test
    void filter() {
        Parser<Character> aParser = Parsers.charParser();

        Optional<ParseResult<Character>> successResult = aParser.filter(c -> c == 'a').parse("abcd");
        assertEquals('a', successResult.get().value());

        var failedResult = aParser.filter(c -> c == 'b').parse("abcd");
        assertEquals(failedResult, Optional.empty());
    }

    @Test
    void map() {
        Parser<Character> aParser = Parsers.charParser();

        var result = aParser.map(c -> Character.getNumericValue(c)).parse("abcd");
        assertEquals(result.orElseThrow().value(), Character.getNumericValue('a'));
    }

    @Test
    void flatMap() {
        Parser<Character> aParser = Parsers.charParser();

        Parser<Character> numParser = aParser.flatMap(c -> Parsers.digit());
        assertEquals('1', numParser.parse("21").orElseThrow().value());
    }
}