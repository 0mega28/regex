package com.example.regex.matcher;

import com.example.regex.Regex;

import java.util.Optional;

@FunctionalInterface
public interface Matching {
    Optional<Regex.Match> nextMatch();
}
