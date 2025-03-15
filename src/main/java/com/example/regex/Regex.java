package com.example.regex;

import com.example.regex.ast.AST;
import com.example.regex.compiler.CompiledRegex;
import com.example.regex.compiler.CompiledState;
import com.example.regex.compiler.Compiler;
import com.example.regex.grammar.Grammar;
import com.example.regex.matcher.BacktrackingMatcher;
import com.example.regex.matcher.Matching;
import com.example.regex.matcher.RegularMatcher;
import com.example.regex.optimizer.Optimizer;
import com.example.regex.parser.ParseException;
import com.example.regex.parser.ParseResult;
import com.example.regex.parser.Parser;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class Regex {
    private static final Logger LOGGER = Logger.getLogger(Regex.class.getName());
    public static final boolean DEBUG_ENABLED = true;
    private final CompiledRegex regex;
    private final List<Options> options;

    public static class Options {
    }

    public Regex(String pattern, List<Options> options) {
        AST ast = parse(pattern);
        AST optimizedAst = new Optimizer().optimize(ast);
        this.regex = new Compiler(optimizedAst, options).compile();
        this.options = options;

        LOGGER.info(() -> "AST: \n" + ast.getDescription());
        LOGGER.info(() -> "AST (Optimized): \n" + optimizedAst.getDescription());
        LOGGER.info(() -> "Expression: \n" + regex.symbols().description(new CompiledState(0)));
    }

    public Regex(String pattern) {
        this(pattern, List.of());
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

    public class Match {
    }
}
