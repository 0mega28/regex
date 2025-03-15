package com.example.regex.compiler;

public record CompiledCaptureGroup(int index, CompiledState start, CompiledState end) {
}
