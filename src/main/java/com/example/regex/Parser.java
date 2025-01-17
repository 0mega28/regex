package com.example.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

record ParseResult<A>(A value, String remaining) {
}

class ParseException extends RuntimeException {
}

public class Parser<A> {
    @FunctionalInterface
    interface ParseFunction<A> {
        Optional<ParseResult<A>> parse(String input) throws ParseException;
    }

    private final ParseFunction<A> parseFunction;

    Parser(ParseFunction<A> parseFunction) {
        this.parseFunction = parseFunction;
    }

    Optional<ParseResult<A>> parse(String input) throws ParseException {
        return parseFunction.parse(input);
    }

    public Parser<List<A>> zeroOrMore() {
        return new Parser<>(input -> {
            List<A> matches = new ArrayList<>();
            String remaining = input;

            while (true) {
                Optional<ParseResult<A>> result = parse(remaining);
                if (result.isEmpty()) break;
                matches.add(result.get().value());
                remaining = result.get().remaining();
            }

            return Optional.of(new ParseResult<>(matches, remaining));
        });
    }

    public Parser<List<A>> oneOrMore() {
        return zeroOrMore().filter(Predicate.not(List::isEmpty));
    }

    public Parser<A> filter(Predicate<A> predicate) {
        return new Parser<>(input -> {
            return parseFunction.parse(input)
                    .filter(parseResult -> predicate.test(parseResult.value()));
        });
    }

    public <B> Parser<B> map(Function<A, B> mapper) {
        return new Parser<>(input -> parseFunction.parse(input)
                .map(parseResult -> new ParseResult<B>(mapper.apply(parseResult.value()), parseResult.remaining())));
    }

    public <B> Parser<B> flatMap(Function<A, Parser<B>> mapper) {
        return new Parser<>(input -> parseFunction.parse(input)
                .flatMap(
                        parseResult -> mapper.apply(parseResult.value()).parseFunction.parse(parseResult.remaining())));
    }
}

class Parsers {
    public static Parser<Void> string(String p) {
        return new Parser<Void>(input -> input.startsWith(p)
                ? Optional.of(new ParseResult<>(null, input.substring(p.length())))
                : Optional.empty());
    }

    public static Parser<Character> charParser() {
        return new Parser<Character>(input -> !input.isEmpty()
                ? Optional.of(new ParseResult<>(input.charAt(0), input.substring(1)))
                : Optional.empty());

    }

    public static Parser<Character> digit() {
        return charParser().filter(Character::isDigit);
    }

    public static Parser<Integer> number() {
        return digit().oneOrMore().map(characters -> Integer.parseInt(
                characters.stream().map(String::valueOf).collect(Collectors.joining()))
        );
    }
}
