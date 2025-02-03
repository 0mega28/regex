package com.example.regex.fsm;

import com.example.regex.ast.CharacterGroup;
import com.example.regex.ast.CharacterGroup.Item.character;
import com.example.regex.ast.CharacterGroup.Item.range;
import com.example.regex.ast.CharacterGroup.Item.set;
import com.example.regex.util.CharacterSet;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Logger;

import static com.example.regex.fsm.Transition.epsilon;
import static com.example.regex.util.CharacterSet.*;

interface FSMUtil {
    static CharacterSet makeCharacterSet(List<CharacterGroup.Item> items) {
        return items.stream()
                .reduce(CharacterSet.empty(),
                        (set, item) -> switch (item) {
                            case character(char chr) -> set.union(fromChar(chr));
                            case range(var range) -> set.union(fromRange(range));
                            case set(var otherSet) -> set.union(otherSet);
                        },
                        CharacterSet::union);
    }
}

public final class FSM {
    private static final Logger LOGGER = Logger.getLogger(FSM.class.getName());
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
        fsm.start.addTransition(epsilon(fsm.end));
        return fsm;
    }

    // Character classes
    public static FSM character(char c, boolean ignoreCase) {
        return new FSM(new MatchCharacter(c, ignoreCase));
    }

    public static FSM string(String s, boolean ignoreCase) {
        assert !s.isEmpty();
        return new FSM(new MatchString(s, s.length(), ignoreCase));
    }

    public static FSM characterGroup(CharacterGroup group, boolean ignoreCase) {
        if (group.items().isEmpty()) {
            throw new IllegalArgumentException("Group is empty");
        }

        if (group.items().size() == 1 && group.items().getFirst() instanceof range) {
            // TODO in swift code they have a special handling for single GroupItem when it is range
            LOGGER.info("No special handling for range");
        }
        CharacterSet set = FSMUtil.makeCharacterSet(group.items());
        // TODO in swift code they have a special handling for set if it is decimalDigit and they have
        //  ignored isInverted flag in that case
        if (set == decimalDigit) {
            LOGGER.info("No special handling for decimalDigit");
        }
        return new FSM(new MatchCharacterSet(set, ignoreCase, group.isInverted()));
    }

    public static FSM anyCharacter(boolean includingNewLine) {
        return new FSM(new MatchAnyCharacter(includingNewLine));
    }


    // Quantifiers
    public static FSM zeroOrMore(FSM child, boolean isLazy) {
        /*
        child       => cs(a -> b -> c)*ce
        quantifier  => qs -> qe

        qs ---eps---> cs
        cs ---eps---> qe

        effectively
                   |------------v
            qs -> cs (...) ce   qe
                   ^------–-|

        if lazy
            first skip the quantifier hence reverse child.start transition

        ce ---eps---> cs
         */
        FSM quantifier = new FSM();

        quantifier.start.setTransitions(List.of(
                epsilon(child.start)));
        child.start.addTransition(epsilon(quantifier.end));
        // TODO should it be add Transition or set Transition?
        child.end.setTransitions(List.of(
                epsilon(child.start)));
        if (isLazy) {
            child.start.reverseTransition();
        }

        return quantifier;
    }

    public static FSM oneOrMore(FSM child, boolean isLazy) {
        FSM quantifier = new FSM();
        /*
        child       =>  cs (...)+ ce
        quantifier  =>  qs qe

        qs -> cs (...)+ ce -> qe
               ^--------|

        if lazy
            first ce should go to qe
         */

        quantifier.start.setTransitions(List.of(
                epsilon(child.start)));
        child.end.setTransitions(List.of(
                epsilon(child.start),
                epsilon(quantifier.end)));

        if (isLazy)
            child.end.reverseTransition();

        return quantifier;
    }

    public static FSM zeroOrOne(FSM child, boolean isLazy) {
        FSM quantifier = new FSM();
        /*
        child       => cs (...)? ce
        quantifier  => qs qe

        qs -> cs (...)? ce -> qe
         |--------------------^
         */
        quantifier.start.setTransitions(List.of(
                epsilon(child.start),
                epsilon(quantifier.end)));

        child.end.setTransitions(List.of(epsilon(quantifier.end)));

        if (isLazy)
            quantifier.start.reverseTransition();

        return quantifier;
    }

    // Anchors
    private static FSM anchor(Predicate<Cursor> condition) {
        FSM anchor = new FSM();
        anchor.start.setTransitions(List.of(
                epsilon(anchor.end, condition))
        );
        return anchor;
    }

    /**
     * Matches the beginning of the line
     */
    public static FSM startOfString() {
        LOGGER.info("Copied the logic from swift code, please check");
        return anchor(cursor -> cursor.isFromStart() ||
                                cursor.character().isEmpty() || cursor.character().get() == '\n');
    }

    /**
     * Matches the beginning of the string (ignore '.multiline' option)
     */
    public static FSM startOfStringOnly() {
        return anchor(Cursor::isFromStart);
    }

    /**
     * Matches the end of the string or '\n' at the end of the string
     * (end of the line in '.multiline' mode)
     */
    public static FSM endOfString() {
        return anchor(cursor -> cursor.character().isEmpty() || cursor.character().get() == '\n');
    }

    /**
     * Matches the end of the string or '\n' at the end of the string
     */
    public static FSM endOfStringOnly() {
        return anchor(cursor -> cursor.character().isEmpty() ||
                                (cursor.isAtLastIndex() && cursor.character().get() == '\n'));
    }

    /**
     * Matches the end of the string or '\n' at the end of the string (ignores '.multiline' option).
     */
    public static FSM endOfStringOnlyNotNewLine() {
        return anchor(cursor -> cursor.character().isEmpty());
    }

    /**
     * Matches must occur at the point where the previous match ended.
     * Ensures that all matches are contiguous.
     */
    public static FSM previousMatchEnd() {
        return anchor(cursor -> cursor.isAtStart() ||       // There couldn't be any matches before the start
                                cursor.getPreviousMatchIndex().isPresent() ||
                                cursor.getPreviousMatchIndex().getAsInt() == cursor.getIndex());
    }

    /**
     * The match must occur on a word boundary
     */
    public static FSM wordBoundary() {
        return anchor(Cursor::isAtWordBoundary);
    }

    /**
     * The match must occur on a non-word boundary
     */
    public static FSM nonWordBoundary() {
        return anchor(Predicate.not(Cursor::isAtWordBoundary));
    }

}

