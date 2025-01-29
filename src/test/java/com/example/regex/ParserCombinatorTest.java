package com.example.regex;

import org.junit.jupiter.api.Test;

import java.lang.foreign.Linker.Option;
import java.util.Optional;
import java.util.OptionalInt;

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
        record RangeQuantifier(int lowerBound, int upperBound) {
        }
        var rangeParser = zip(string("{"), number(), string(","), number(), string("}"))
                .map(result -> new RangeQuantifier(result.secondValue(), result.fourthValue()));

        assertEquals(new RangeQuantifier(1, 3), rangeParser.parse("{1,3}").orElseThrow().value());
        assertEquals(Optional.empty(), rangeParser.parse("{1,3"));

        var rangeParserWithDot = zip(string("{"), number(), string(","), number(), string("}"), string("."))
                .map(result -> new RangeQuantifier((Integer) result.get(1), (Integer) result.get(3)));

        assertEquals(new RangeQuantifier(1, 3), rangeParserWithDot.parse("{1,3}.").orElseThrow().value());
        assertEquals(Optional.empty(), rangeParserWithDot.parse("{1,3}"));
    }

    @Test
    void zipWithOptional() {
        record RangeQuantifier(int lowerBound, OptionalInt upperBound) {
        }
        var rangeParser = zip(string("{"), number(), string(","), optional(number()), string("}"))
                .map(result -> new RangeQuantifier(result.secondValue(),
                        result.fourthValue().map(OptionalInt::of).orElse(OptionalInt.empty())));

        assertEquals(new RangeQuantifier(1, OptionalInt.of(3)), rangeParser.parse("{1,3}").orElseThrow().value());
        assertEquals(new RangeQuantifier(1, OptionalInt.empty()), rangeParser.parse("{1,}").orElseThrow().value());
        assertEquals(Optional.empty(), rangeParser.parse("{1,3"));
    }

    @Test
    void zipWithChooseCombinator() {
        record RangeQuantifier(Integer lowerBound, Optional<Integer> upperBound) {
        }

        var rangeParser = zip(
                first(
                        second(string("{"), number()), string(",")),
                first(optional(number()), string("}")))
                .map(result -> new RangeQuantifier(result.firstValue(), result.secondValue()));

        assertEquals(new RangeQuantifier(1, Optional.of(3)), rangeParser.parse("{1,3}").orElseThrow().value());
        assertEquals(new RangeQuantifier(1, Optional.empty()), rangeParser.parse("{1,}").orElseThrow().value());
        assertEquals(Optional.empty(), rangeParser.parse("{1,3"));
    }

    @Test
    void testThrows() {
        record RangeQuantifier(int lowerBound, Optional<Integer> upperBound) {
        }

        var rangeParser = zip(
                first(
                        second(string("{"), number().orThrow("Range missing lower bound")), string(",")),
                first(optional(number()), string("}")))
                .map(result -> new RangeQuantifier(result.firstValue(), result.secondValue()));

        assertEquals(new RangeQuantifier(1, Optional.of(3)), rangeParser.parse("{1,3}").orElseThrow().value());
        assertThrows(ParseException.class, () -> rangeParser.parse("{,3}").orElseThrow().value());
    }

    @Test
    void oneOfTest() {
        var parser = oneOf(string("a"), string("b"), string("c"));
        assertEquals(new ParseResult<>(null, "bcd"), parser.parse("abcd").orElseThrow());
        assertEquals(new ParseResult<>(null, "cd"), parser.parse("bcd").orElseThrow());
        assertEquals(new ParseResult<>(null, "d"), parser.parse("cd").orElseThrow());
        assertEquals(Optional.empty(), parser.parse("d"));
    }

    @Test
    void optionalTest() {
        Parser<Boolean> optionalParser = ParserCombinators.optionalb(Parsers.string("abc"));

        // Test case 1: Input is "abc"
        Optional<ParseResult<Boolean>> result = optionalParser.parse("abc");
        assertTrue(result.isPresent());
        assertEquals(new ParseResult<>(true, ""), result.orElseThrow());

        // Test case 2: Input is "abcdef"
        result = optionalParser.parse("abcdef");
        assertTrue(result.isPresent());
        assertEquals(new ParseResult<>(true, "def"), result.orElseThrow());

        // Test case 3: Input is "def"
        result = optionalParser.parse("def");
        assertTrue(result.isPresent());
        assertEquals(new ParseResult<>(false, "def"), result.orElseThrow());
    }
}
