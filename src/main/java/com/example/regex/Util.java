package com.example.regex;

import java.util.List;
import java.util.stream.Collectors;

public interface Util {
    static String characterListToString(List<Character> characters) {
        return characters.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }
}
