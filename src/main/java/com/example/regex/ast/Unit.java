package com.example.regex.ast;

public sealed interface Unit permits
        Alternation,
        Anchor,
        BackReference,
        CharacterGroup,
        Group,
        ImplicitGroup,
        Match,
        QuantifiedExpression {
}
