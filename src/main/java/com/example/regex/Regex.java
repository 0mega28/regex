package com.example.regex;

import com.example.regex.ast.AST;
import com.example.regex.compiler.CompiledRegex;
import com.example.regex.compiler.CompiledState;
import com.example.regex.compiler.Compiler;
import com.example.regex.fsm.Cursor;
import com.example.regex.grammar.Grammar;
import com.example.regex.matcher.BacktrackingMatcher;
import com.example.regex.matcher.Matching;
import com.example.regex.matcher.RegularMatcher;
import com.example.regex.optimizer.Optimizer;
import com.example.regex.parser.ParseException;
import com.example.regex.parser.ParseResult;
import com.example.regex.parser.Parser;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class Regex {
    private static final Logger LOGGER = Logger.getLogger(Regex.class.getName());
    public static final boolean DEBUG_ENABLED = true;
    private final CompiledRegex regex;
    private final Options options;

    public static class Options {
        private final HashSet<Option> optionSet;

        public Options() {
            optionSet = new HashSet<>();
        }

        public enum Option {
            CASE_INSENSITIVE,
            MULTILINE,
            DOT_MATCHES_LINE_SEPARATORS,
        }

        public boolean contains(Option option) {
            return optionSet.contains(option);
        }
    }

    public Regex(String pattern, Options options) {
        AST ast = parse(pattern);
        AST optimizedAst = new Optimizer().optimize(ast);
        this.regex = new Compiler(optimizedAst, options).compile();
        this.options = options;

        LOGGER.info(() -> "AST: \n" + ast.description());
        LOGGER.info(() -> "AST (Optimized): \n" + optimizedAst.description());
        LOGGER.info(() -> "Expression: \n" + regex.symbols().description(new CompiledState(0)));
    }

    public Regex(String pattern) {
        this(pattern, new Options());
    }

    static AST parse(String pattern) {
        Parser<AST> parser = Grammar.REGEX;
        Optional<ParseResult<AST>> parse = parser.parse(pattern);
        return  parse.orElseThrow(() -> new ParseException("Unexpected Error")).value();
    }

    boolean isMatch(String string) {
        var matcher = makeMatcher(string, true);
        return matcher.nextMatch().isPresent();
    }

    private Matching makeMatcher(String string, boolean isMatchOnly) {
        if (regex.isRegular()) {
            return new RegularMatcher(string, regex, options, isMatchOnly);
        } else {
            return new BacktrackingMatcher(string, regex, options, isMatchOnly);
        }
    }

    public static class Match {
        String fullMatch;
        List<String> groups;
        int endIndex;

        Match(Cursor cursor, boolean hasCaptureGroups) {
            fullMatch = cursor.substring(cursor.startIndex(), cursor.index()).orElseThrow();

            if (hasCaptureGroups) {
                this.groups = cursor.groups()
                        .entrySet()
                        .stream()
                        .sorted(Comparator.comparingInt(e -> e.getKey()))
                        .map(e -> cursor.substring(e.getValue()).orElseThrow())
                        .toList();
            } else {
                this.groups = List.of();
            }
            this.endIndex = cursor.index();
        }

        public String fullMatch() {
            return fullMatch;
        }

        public int endIndex() {
            return endIndex;
        }

        public List<String> groups() {
            return List.copyOf(groups);
        }
    }
}
