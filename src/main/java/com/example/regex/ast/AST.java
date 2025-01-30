package com.example.regex.ast;

import java.util.function.BiConsumer;

public record AST(boolean isFromStartOfString, Unit root) {
    @Override
    public String toString() {
        return getDescription();
    }

    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        BiConsumer<Unit, Integer> visitor = (unit, level) -> {
            String indent = " ".repeat(level * 2);
            sb.append(indent).append("- ").append(unit).append("\n");
        };

        visit(root, 0, visitor);

        return sb.toString();
    }

    private void visit(Unit unit, int level, BiConsumer<Unit, Integer> visitor) {
        visitor.accept(unit, level);
        if (unit instanceof Composite composite) {
            for (Unit child : composite.children()) {
                visit(child, level + 1, visitor);
            }
        }
    }
}

