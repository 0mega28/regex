package com.example.regex.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public record Parser<A>(ParseFunction<A> parseFunction) {
    public Optional<ParseResult<A>> parse(String input) throws ParseException {
        return parseFunction.parse(input);
    }

    public Parser<List<A>> zeroOrMore() {
        return new Parser<>(input -> {
            List<A> matches = new ArrayList<>();
            String remaining = input;

            while (true) {
                Optional<ParseResult<A>> result = parse(remaining);
                if (result.isEmpty())
                    break;
                matches.add(result.get().value());
                remaining = result.get().remaining();
            }

            return Optional.of(new ParseResult<>(matches, remaining));
        });
    }

    public Parser<List<A>> oneOrMore() {
        return zeroOrMore().filter(Predicate.not(List::isEmpty));
    }

    public Parser<A> zeroOrThrow(String message) {
        return map(_ignore -> {
            throw new ParseException(message);
        });
    }

    public Parser<A> filter(Predicate<? super A> predicate) {
        return new Parser<>(input -> parseFunction.parse(input)
                .filter(parseResult -> predicate.test(parseResult.value())));
    }

    public <B> Parser<B> map(Function<? super A, ? extends B> mapper) {
        return new Parser<>(input -> parseFunction.parse(input)
                .map(parseResult -> new ParseResult<>(mapper.apply(parseResult.value()), parseResult.remaining())));
    }

    @SuppressWarnings("unchecked")
    public <B> Parser<B> flatMap(Function<? super A, ? extends Parser<? extends B>> mapper) {
        return new Parser<>(input -> parseFunction.parse(input)
                .flatMap(
                        parseResult -> mapper.apply(parseResult.value())
                                .parseFunction().parse(parseResult.remaining())
                                .map(result -> (ParseResult<B>) result)));
    }

    public Parser<A> orThrow(String message) {
        return new Parser<>(input -> parseFunction.parse(input)
                .or(() -> {
                    throw new ParseException(message);
                }));
    }

    @FunctionalInterface
    interface ParseFunction<A> {
        Optional<ParseResult<A>> parse(String input) throws ParseException;
    }
}

