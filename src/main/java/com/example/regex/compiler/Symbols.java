package com.example.regex.compiler;

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

    public String description(CompiledState state) {
        /*
            func description(for state: CompiledState) -> String {
            #if DEBUG
            let details = map[state]

            let info: String? = details.flatMap {
                return "\($0.isEnd ? "End" : "Start"), \(ast.description(for: $0.unit))"
            }

            return "\(state) [\(info ?? "<symbol missing>")]"
            #else
            return "\(state) [<symbol missing>]"
            #endif
        }
         */
        throw new UnsupportedOperationException();
    }
}
