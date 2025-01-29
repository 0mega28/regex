package com.example.regex;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

interface Unit {
}

interface Composite {
    List<Unit> children();
}

record AST(boolean isFromStartOfString, Unit root) {
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

record Group(Optional<Integer> index, boolean isCapturing, List<Unit> children) implements Unit, Composite {
    Group {
        children = List.copyOf(children);
    }
}

record ImplicitGroup(List<Unit> children) implements Unit, Composite {
    ImplicitGroup {
        children = List.copyOf(children);
    }
}

record Alternation(List<Unit> children) implements Unit, Composite {
    Alternation {
        children = List.copyOf(children);
    }
}

record BackReference(int index) implements Unit {
}

enum Anchor implements Unit {
    START_OF_STRING,
    END_OF_STRING,
    WORD_BOUNDARY,
    NON_WORD_BOUNDARY,
    START_OF_STRING_ONLY,
    END_OF_STRING_ONLY,
    END_OF_STRING_ONLY_NOT_NEWLINE,
    PREVIOUS_MATCH_END;

    @Override
    public String toString() {
        return "Anchor." + name().toLowerCase();
    }
}

interface Match extends Unit {
    record AnyCharacter() implements Match {
    }

    record Character(char character) implements Match {
    }

    record String(String string) implements Match {
    }

    record Set(CharacterSet set) implements Match {
    }

    record Group(CharacterGroup group) implements Match {
    }
}

record CharacterGroup(boolean isInverted,
        List<Item> items) implements Unit {
    interface Item {
        record Character(char character) implements Item {
        }

        record Range(char start, char end) implements Item {
        }

        record Set(CharacterSet set) implements Item {
        }
    }

    CharacterGroup {
        items = List.copyOf(items);
    }
}

record QuantifiedExpression(Unit expression,
        Quantifier quantifier) implements Unit, Composite {

    @Override
    public List<Unit> children() {
        return List.of(expression);
    }
}

record Quantifier(Type type, boolean isLazy) {
    interface Type {
        record ZeroOrMore() implements Quantifier.Type {
        }

        record OneOrMore() implements Quantifier.Type {
        }

        record ZeroOrOne() implements Quantifier.Type {
        }

        record Range(int lowerBound, Optional<Integer> upperBound) implements Quantifier.Type {
        }
    }
}
