package com.example.regex.ast;

import java.util.List;

public record Alternation(List<Unit> children) implements Unit, Composite {
    public Alternation {
        children = List.copyOf(children);
    }
}
