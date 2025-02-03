package com.example.regex.ast;

import java.util.Optional;

public record Quantifier(Type type, boolean isLazy) {
    public sealed interface Type {
        record zeroOrMore() implements Type {
        }

        record oneOrMore() implements Type {
        }

        record zeroOrOne() implements Type {
        }

        record range(int lowerBound, Optional<Integer> upperBound) implements Type {
        }
    }
}
