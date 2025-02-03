package com.example.regex.ast;

import com.example.regex.util.CharacterSet;
import com.example.regex.util.Range;

import java.util.List;

public record CharacterGroup(boolean isInverted,
                             List<Item> items) implements Unit {
    public CharacterGroup {
        items = List.copyOf(items);
    }

    public sealed interface Item {
        record character(char character) implements Item {
        }

        record range(Range<Character> characterRange) implements Item {
        }

        record set(CharacterSet set) implements Item {
        }
    }
}
