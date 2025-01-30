package com.example.regex.ast;

import java.util.List;
import java.util.Optional;

public record Group(Optional<Integer> index, boolean isCapturing, List<Unit> children) implements Unit, Composite {
    public Group {
        children = List.copyOf(children);
    }
}
