package com.example.regex.ast;

import com.example.regex.util.CharacterSet;

public sealed interface Match extends Unit {
    record anyCharacter() implements Match {
    }

    record character(char character) implements Match {
    }

    record string(String string) implements Match {
    }

    record set(CharacterSet set) implements Match {
    }

    record group(CharacterGroup group) implements Match {
    }
}
