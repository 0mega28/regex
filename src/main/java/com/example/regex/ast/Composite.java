package com.example.regex.ast;

import java.util.List;

public sealed interface Composite permits
        Alternation,
        Group,
        ImplicitGroup,
        QuantifiedExpression {
    List<Unit> children();
}
