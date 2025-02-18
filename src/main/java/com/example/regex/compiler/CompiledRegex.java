package com.example.regex.compiler;

import java.util.List;

public record CompiledRegex(
    CompiledStateMachine fsm,
    List<CompiledCaptureGroup> captureGroups,
    boolean isRegular,
    boolean isFromStartOfString,
    Symbols symbols
) {
}
