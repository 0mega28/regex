package com.example.regex.compiler;

import java.util.List;

public record CompiledStateMachine(List<List<CompiledTransition>> transitions) {
}
