package com.example.regex.ast;

import java.util.List;

public record ImplicitGroup(List<Unit> children) implements Unit, Composite {
    public ImplicitGroup {
        children = List.copyOf(children);
    }
}
