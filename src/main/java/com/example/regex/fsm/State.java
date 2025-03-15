package com.example.regex.fsm;

import com.example.regex.compiler.Compiler;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public final class State {
    private final List<Transition> transitions;

    public State() {
        this.transitions = new ArrayList<>();
    }

    public void addTransition(Transition transition) {
        transitions.add(transition);
    }

    public void reverseTransition() {
        Collections.reverse(transitions);
    }

    public void setTransitions(List<Transition> transitions) {
        this.transitions.clear();
        this.transitions.addAll(transitions);
    }

    public void visit(BiConsumer<State, Integer> closure) {
        record StateLevel(State state, Integer level) {};
        Queue<StateLevel> queue = new ArrayDeque<>();
        Set<State> visited = new HashSet<>();
        queue.offer(new StateLevel(this, 0));

        while (!queue.isEmpty()) {
            StateLevel stateLevel = queue.poll();
            State curr = stateLevel.state;
            Integer level = stateLevel.level;
            if (visited.contains(curr)) continue;
            visited.add(curr);
            closure.accept(curr, level);

            curr
                    .transitions
                    .stream()
                    .map(Transition::end)
                    .filter(Predicate.not(visited::contains))
                    .map(end -> new StateLevel(end, level + 1))
                    .forEach(queue::offer);

        }
    }

    public List<Transition> transition() {
        return List.copyOf(transitions);
    }

    public void clearTransition() {
        transitions.clear();
    }
}
