package com.example.regex.parser;

public record ParseResult<A>(A value, String remaining) {
}
