package com.example.regex.parser;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Parsers {
    public static Parser<Void> string(String p) {
        return new Parser<>(input -> input.startsWith(p)
                ? Optional.of(new ParseResult<>(null, input.substring(p.length())))
                : Optional.empty());
    }

    public static Parser<Character> charParser() {
        return new Parser<>(input -> !input.isEmpty()
                ? Optional.of(new ParseResult<>(input.charAt(0), input.substring(1)))
                : Optional.empty());

    }

    public static Parser<Character> charExcluding(String excluded) {
        return charParser().filter(c -> excluded.indexOf(c) == -1);
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
