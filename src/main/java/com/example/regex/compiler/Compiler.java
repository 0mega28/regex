package com.example.regex.compiler;

import com.example.regex.Regex;
import com.example.regex.ast.*;
import com.example.regex.fsm.FSM;
import com.example.regex.fsm.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Compiler {
    private final AST ast;
    private final Regex.Options options;
    private Map<State, Symbols.Details> map;
    private final List<IRCaptureGroup> captureGroups;
    private final List<BackReference> backReferences;

    Compiler(AST ast, Regex.Options options) {
        this.ast = ast;
        this.options = options;
        captureGroups = new ArrayList<>();
        backReferences = new ArrayList<>();
    }

    CompiledRegex _compile() {
        throw new UnsupportedOperationException();
    }

    private FSM compile(Unit unit) {
        FSM fsm = _compile(unit);
        if (Regex.DEBUG_ENABLED) {
            assert fsm != null;
            map.putIfAbsent(fsm.start(), new Symbols.Details(unit, false));
            map.putIfAbsent(fsm.end(), new Symbols.Details(unit, true));
        }
        return fsm;
    }

    private FSM _compile(Unit unit) {
        return switch (unit) {
            case ImplicitGroup expression -> compile(expression);
            case Group group -> compile(group);
            case BackReference backReference -> compile(backReference);
            case Alternation alternation -> compile(alternation);
            case Anchor anchor -> compile(anchor);
            case QuantifiedExpression quantifiedExpression -> compile(quantifiedExpression);
            case Match match -> compile(match);
            case CharacterGroup characterGroup -> throw new RuntimeException("Unsupported unit CharacterGroup");
        };
    }

    private FSM compile(ImplicitGroup expression) {
        return FSM.concatenate(expression.children().stream().map(this::compile).toList());
    }

    private FSM compile(Group group) {
        List<FSM> fsms = group.children().stream().map(this::compile).toList();
        FSM fsm = FSM.group(FSM.concatenate(fsms));

        if (group.isCapturing()) {
            captureGroups.add(new IRCaptureGroup(group.index().orElseThrow(),
                            fsm.start(),
                            fsm.end()));
        }

        return fsm;
    }

    private FSM compile(BackReference backReference) {
        backReferences.add(backReference);
        return FSM.backreference(backReference.index());
    }

}
