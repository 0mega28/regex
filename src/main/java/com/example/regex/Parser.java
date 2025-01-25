package com.example.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

record ParseResult<A>(A value, String remaining) {
}

record Double<A, B>(A firstValue, B secondValue) {
}

record Triple<A, B, C>(A firstValue, B secondValue, C thirdValue) {
}

record Quadruple<A, B, C, D>(A firstValue, B secondValue, C thirdValue, D fourthValue) {
}

record Quintiple<A, B, C, D, E>(A firstValue, B secondValue, C thirdValue, D fourthValue, E fifthValue) {
}

class ParseException extends RuntimeException {

    public ParseException(String message) {
        super(message);
    }
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

    public Parser<A> orThrow(String message) {
        return new Parser<>(input -> parseFunction.parse(input)
                .or(() -> {
                    throw new ParseException(message);
                }));
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

    public static Parser<Character> charExcluding(String exluded) {
        return charParser().filter(c -> exluded.indexOf(c) == -1);
    }

    public static Parser<Character> charFrom(String allowed) {
        return charParser().filter(c -> allowed.indexOf(c) != -1);
    }

    public static Parser<List<Character>> stringExcluding(String excluded) {
        return charExcluding(excluded).oneOrMore();
    }

    public static Parser<Character> digit() {
        return charParser().filter(Character::isDigit);
    }

    public static Parser<Integer> number() {
        return digit().oneOrMore().map(characters -> Integer.parseInt(
                characters.stream().map(String::valueOf).collect(Collectors.joining())));
    }

    public static <A> Parser<A> lazy(Supplier<Parser<A>> parserSupplier) {
        return new Parser<>(input -> parserSupplier.get().parse(input));
    }

    public static Parser<Void> end() {
        return new Parser<>(input -> input.isEmpty()
                ? Optional.of(new ParseResult<>(null, input))
                : Optional.empty());
    }
}

class ParserCombinators {
    public static <A, B> Parser<Double<A, B>> zip(Parser<A> first, Parser<B> second) {
        return first
                .flatMap(firstParseResult -> second.map(secondParseResult -> new Double<>(firstParseResult,
                        secondParseResult)));
    }

    public static <A, B, C> Parser<Triple<A, B, C>> zip(Parser<A> first, Parser<B> second, Parser<C> third) {
        return zip(first, zip(second, third))
                .map(result -> new Triple<>(result.firstValue(), result.secondValue().firstValue(),
                        result.secondValue().secondValue()));
    }

    public static <A, B, C, D> Parser<Quadruple<A, B, C, D>> zip(Parser<A> first, Parser<B> second, Parser<C> third,
            Parser<D> forth) {
        return zip(zip(first, second), zip(third, forth))
                .map(result -> new Quadruple<>(result.firstValue().firstValue(), result.firstValue().secondValue(),
                        result.secondValue().firstValue(), result.secondValue().secondValue()));
    }

    public static <A, B, C, D, E> Parser<Quintiple<A, B, C, D, E>> zip(Parser<A> first, Parser<B> second,
            Parser<C> third, Parser<D> forth, Parser<E> fifth) {
        return zip(zip(first, second, third), zip(forth, fifth))
                .map(result -> new Quintiple<>(result.firstValue().firstValue(), result.firstValue().secondValue(),
                        result.firstValue().thirdValue(), result.secondValue().firstValue(),
                        result.secondValue().secondValue()));
    }

    public static Parser<List<Object>> zip(Parser<?>... parsers) {
        return new Parser<>(input -> {
            List<Object> result = new ArrayList<>();
            String remaining = input;

            for (Parser<?> parser : parsers) {
                Optional<? extends ParseResult<?>> parseResult = parser.parse(remaining);
                if (parseResult.isEmpty())
                    return Optional.empty();
                result.add(parseResult.get().value());
                remaining = parseResult.get().remaining();
            }

            return Optional.of(new ParseResult<>(result, remaining));
        });
    }

    public static <A> Parser<Optional<A>> optional(Parser<A> parser) {
        return new Parser<>(input -> {
            Optional<ParseResult<A>> result = parser.parse(input);
            return Optional.of(new ParseResult<>(
                    result.map(ParseResult::value),
                    result.map(ParseResult::remaining).orElse(input)));
        });
    }

    public static <A, B> Parser<B> chooseSecond(Parser<A> first, Parser<B> second) {
        return zip(first, second).map(result -> result.secondValue());
    }

    public static <A, B> Parser<A> chooseFirst(Parser<A> first, Parser<B> second) {
        return zip(first, second).map(result -> result.firstValue());
    }

    @SafeVarargs
    public static <A> Parser<A> oneOf(Parser<A>... parsers) {
        return new Parser<>(input -> {
            for (Parser<A> parser : parsers) {
                Optional<ParseResult<A>> result = parser.parse(input);
                if (result.isPresent())
                    return result;
            }
            return Optional.empty();
        });
    }
}
