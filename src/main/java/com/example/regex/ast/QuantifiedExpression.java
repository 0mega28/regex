package com.example.regex.ast;

import java.util.List;

public record QuantifiedExpression(Unit expression,
                                   Quantifier quantifier) implements Unit, Composite {

    @Override
    public List<Unit> children() {
        return List.of(expression);
    }
}
