package com.example.regex.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ParserCombinators {
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

    public static <A, B, C, D, E> Parser<Quintuple<A, B, C, D, E>> zip(Parser<A> first, Parser<B> second,
                                                                       Parser<C> third, Parser<D> forth, Parser<E> fifth) {
        return zip(zip(first, second, third), zip(forth, fifth))
                .map(result -> new Quintuple<>(result.firstValue().firstValue(), result.firstValue().secondValue(),
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

    /**
     * Should not be used with {@link Parsers#string(String)} parser
     * it'll throw {@link RuntimeException} since, string parser always
     * return null parsed value.
     */
    public static <A> Parser<Optional<A>> optional(Parser<A> parser) {
        return new Parser<>(input -> {
            Optional<ParseResult<A>> result = parser.parse(input);
            return result.map(aParseResult -> new ParseResult<>(Optional.of(aParseResult.value()), aParseResult.remaining())).or(() -> Optional.of(new ParseResult<>(Optional.empty(), input)));
        });
    }

    public static <A> Parser<Boolean> optionalb(Parser<A> parser) {
        return new Parser<>(input -> {
            Optional<ParseResult<A>> result = parser.parse(input);
            return result.map(aParseResult -> new ParseResult<>(true, aParseResult.remaining())).or(() -> Optional.of(new ParseResult<>(false, input)));
        });
    }

    public static <A, B> Parser<B> second(Parser<A> first, Parser<B> second) {
        return zip(first, second).map(Double::secondValue);
    }

    public static <A, B> Parser<A> first(Parser<A> first, Parser<B> second) {
        return zip(first, second).map(Double::firstValue);
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
