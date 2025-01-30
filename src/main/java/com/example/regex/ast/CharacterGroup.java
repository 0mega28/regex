package com.example.regex.ast;

import com.example.regex.util.CharacterSet;

import java.util.List;

public record CharacterGroup(boolean isInverted,
                             List<Item> items) implements Unit {
    public CharacterGroup {
        items = List.copyOf(items);
    }

    public interface Item {
        record Character(char character) implements Item {
        }

        record Range(char start, char end) implements Item {
        }

        record Set(CharacterSet set) implements Item {
        }
    }
}
