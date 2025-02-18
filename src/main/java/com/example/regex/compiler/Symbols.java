package com.example.regex.compiler;

import com.example.regex.ast.AST;
import com.example.regex.ast.Unit;

public class Symbols {
    static class Details {
        Unit unit;
        boolean isEnd;

        public Details(Unit unit, boolean isEnd) {
            this.unit = unit;
            this.isEnd = isEnd;
        }

    }

}
