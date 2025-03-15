package com.example.regex.matcher;

import com.example.regex.Regex;
import com.example.regex.compiler.CompiledRegex;

import java.util.List;
import java.util.Optional;

public class RegularMatcher implements Matching {
    public RegularMatcher(String string, CompiledRegex regex, List<Regex.Options> options, boolean isMatchOnly) {
    }

    @Override
    public Optional<Regex.Match> nextMatch() {
        return Optional.empty();
    }
}
