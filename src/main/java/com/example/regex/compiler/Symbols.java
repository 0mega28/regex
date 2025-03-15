package com.example.regex.compiler;

import com.example.regex.Regex;
import com.example.regex.ast.AST;
import com.example.regex.ast.Unit;

import java.util.Map;

public class Symbols {
    public static class Details {
        Unit unit;
        boolean isEnd;

        public Details(Unit unit, boolean isEnd) {
            this.unit = unit;
            this.isEnd = isEnd;
        }
    }
    private final AST ast;
    private final Map<CompiledState, Details> map;

    public Symbols(AST ast, Map<CompiledState, Details> map) {
        this.ast = ast;
        this.map = map;
    }

    public Symbols() {
        ast = null;
        map = null;
    }


    public String description(CompiledState state) {
        if (Regex.DEBUG_ENABLED) {
            assert map != null;
            assert ast != null;
            Details details = map.get(state);

            if (details == null) {
                return "(" + state + ") [<symbol missing>]";
            }

            String info = details.isEnd ? "End" : "Start";
            return "(" + state + ") [" + info + ", " + ast.description(details.unit) + "]";
        }

        return "(" + state + ") [<symbol missing>]";
    }
}
