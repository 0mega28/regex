package com.example.regex.fsm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
}
