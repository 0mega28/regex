package com.example.regex.ast;

import com.example.regex.util.CharacterSet;

public interface Match extends Unit {
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
