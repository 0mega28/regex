package com.example.regex.compiler;

import com.example.regex.Regex;
import com.example.regex.Regex.Options;
import com.example.regex.ast.*;
import com.example.regex.fsm.FSM;
import com.example.regex.fsm.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.example.regex.Regex.Options.Option.CASE_INSENSITIVE;
import static com.example.regex.Regex.Options.Option.DOT_MATCHES_LINE_SEPARATORS;

public class Compiler {
    private final AST ast;
    private final Options options;
    private final Map<State, Symbols.Details> map;
    private final List<IRCaptureGroup> captureGroups;
    private final List<BackReference> backReferences;
    private boolean containsLazyQuantifiers = false;

    public Compiler(AST ast, Options options) {
        this.ast = ast;
        this.options = options;
        captureGroups = new ArrayList<>();
        backReferences = new ArrayList<>();
        map = new HashMap<>();
    }

    public CompiledRegex compile() {
        FSM fsm = compile(ast.root());
        optimize(fsm);
        validateBackreferences();
        return preprocess(fsm);
    }

    private CompiledRegex preprocess(FSM fsm) {
        List<State> states = fsm.allStates();
        Map<State, Integer> indices = IntStream.range(0, states.size())
                .boxed()
                .collect(Collectors.toMap(states::get, Function.identity()));

        List<CompiledCaptureGroup> captureGroups = this.captureGroups.stream()
                .map(captureGroup -> new CompiledCaptureGroup(captureGroup.index(),
                        new CompiledState(indices.get(captureGroup.start())),
                        new CompiledState(indices.get(captureGroup.end()))))
                .toList();

        List<List<CompiledTransition>> transitions = states.stream()
                .map(state -> state.transition().stream()
                        .map(transition -> new CompiledTransition(
                                new CompiledState(indices.get(state)), transition.condition()))
                        .toList())
                .toList();

        states.forEach(State::clearTransition);

        Symbols symbols = new Symbols();
        if (Regex.DEBUG_ENABLED) {
            Map<CompiledState, Symbols.Details> details = new HashMap<>();
            for (Map.Entry<State, Symbols.Details> entry : map.entrySet()) {
                State state = entry.getKey();
                if (indices.containsKey(state)) {
                    details.put(new CompiledState(indices.get(state)), entry.getValue());
                }
            }
            symbols = new Symbols(ast, details);
        }
        return new CompiledRegex(new CompiledStateMachine(transitions),
                captureGroups,
                !containsLazyQuantifiers && backReferences.isEmpty(),
                ast.isFromStartOfString(),
                symbols
        );
    }

    private void validateBackreferences() {
        // TODO
    }

    private void optimize(FSM fsm) {
        // TODO
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

    private FSM compile(Alternation alternation) {
        throw new UnsupportedOperationException();
    }

    private FSM compile(Anchor anchor) {
        throw new UnsupportedOperationException();
    }

    private FSM compile(QuantifiedExpression quantifiedExpression) {
        throw new UnsupportedOperationException();
    }

    private FSM compile(Match match) {
        boolean ignoreCase = !options.contains(CASE_INSENSITIVE);
        boolean dotMatchesLineSeparator = options.contains(DOT_MATCHES_LINE_SEPARATORS);

        return switch (match) {
            case Match.character(char c) -> FSM.character(c, ignoreCase);
            case Match.string string -> throw new UnsupportedOperationException();
            case Match.anyCharacter anyCharacter -> throw new UnsupportedOperationException();
            case Match.set set -> throw new UnsupportedOperationException();
            case Match.group group -> throw new UnsupportedOperationException();
        };
    }
}
