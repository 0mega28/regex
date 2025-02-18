package com.example.regex.compiler;

import com.example.regex.fsm.Condition;

public record CompiledTransition(CompiledState end, Condition condition) {
}
