package com.example.regex.compiler;

import com.example.regex.fsm.State;

public record IRCaptureGroup(Integer index, State start, State end) {
}
