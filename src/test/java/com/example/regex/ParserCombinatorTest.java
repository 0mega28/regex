package com.example.regex;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.example.regex.Parsers.*;
import static com.example.regex.ParserCombinators.*;
import static org.junit.jupiter.api.Assertions.*;

class ParserCombinatorTest {

    @Test
    void filter() {
        Parser<Character> aParser = charParser();

        Optional<ParseResult<Character>> successResult = aParser.filter(c -> c == 'a').parse("abcd");
        assertEquals('a', successResult.get().value());

        var failedResult = aParser.filter(c -> c == 'b').parse("abcd");
        assertEquals(failedResult, Optional.empty());
    }

    @Test
    void map() {
        Parser<Character> aParser = charParser();

        var result = aParser.map(c -> Character.getNumericValue(c)).parse("abcd");
        assertEquals(result.orElseThrow().value(), Character.getNumericValue('a'));
    }

    @Test
    void flatMap() {
        Parser<Character> aParser = charParser();

        Parser<Character> numParser = aParser.flatMap(c -> Parsers.digit());
        assertEquals('1', numParser.parse("21").orElseThrow().value());
    }

    @Test
    void zipTest() {
        record RangeQuantifier(int lowerBound, int upperBound) {}
        var rangeParser = zip(string("{"), number(), string(","), number(), string("}"))
            .map(result -> new RangeQuantifier(result.secondValue(), result.fourthValue()));
        
        assertEquals(new RangeQuantifier(1, 3), rangeParser.parse("{1,3}").orElseThrow().value());
        assertEquals(Optional.empty(), rangeParser.parse("{1,3"));

        var rangeParserWithDot = zip(string("{"), number(), string(","), number(), string("}"), string("."))
            .map(result -> new RangeQuantifier((Integer) result.get(1), (Integer) result.get(3)));

        assertEquals(new RangeQuantifier(1, 3), rangeParserWithDot.parse("{1,3}.").orElseThrow().value());
        assertEquals(Optional.empty(), rangeParserWithDot.parse("{1,3}"));
    }
}