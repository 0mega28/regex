package com.example.regex.ast;

import java.util.Optional;

public record Quantifier(Type type, boolean isLazy) {
    public interface Type {
        record ZeroOrMore() implements Type {
        }

        record OneOrMore() implements Type {
        }

        record ZeroOrOne() implements Type {
        }

        record Range(int lowerBound, Optional<Integer> upperBound) implements Type {
        }
    }
}
