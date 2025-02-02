package com.example.regex.fsm;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class State {
    private final List<Transition> transitions;

    public State() {
        this.transitions = new ArrayList<>();
    }

    public void addTransition(Transition transition) {
        transitions.add(transition);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (State) obj;
        return Objects.equals(this.transitions, that.transitions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transitions);
    }

    @Override
    public String toString() {
        return "State[" +
               "transitions=" + transitions + ']';
    }

}
