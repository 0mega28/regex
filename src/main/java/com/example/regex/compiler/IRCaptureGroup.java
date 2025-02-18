package com.example.regex.compiler;

import com.example.regex.fsm.State;

import java.util.Optional;

public record IRCaptureGroup(Integer index, State start, State end) {
}
