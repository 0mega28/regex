package com.example.regex.fsm;

import java.util.Objects;

public final class FSM {
    private final State start;
    private final State end;

    public FSM(State start, State end) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        this.start = start;
        this.end = end;
    }

    public FSM(Condition condition) {
        this(new State(), new State());
        start.addTransition(new Transition(end, condition));
    }

    public FSM() {
        this(new State(), new State());
    }

    /**
     * Creates an empty FSM
     */
    public static FSM empty() {
        FSM fsm = new FSM();
        fsm.start.addTransition(Transition.epsilon(fsm.end));
        return fsm;
    }

    public State start() {
        return start;
    }

    public State end() {
        return end;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (FSM) obj;
        return Objects.equals(this.start, that.start) &&
               Objects.equals(this.end, that.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        return "FSM[" +
               "start=" + start + ", " +
               "end=" + end + ']';
    }

}
