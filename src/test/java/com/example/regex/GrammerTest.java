
package com.example.regex;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class GrammerTest {

    @Test
    public void testQuantified() {
        Parser<Unit> parser = Grammer.quantified(Parsers.string("a")
                .map(value -> (Unit) new Match.Character('a')));

        Optional<ParseResult<Unit>> result1 = parser.parse("a");
        assertTrue(result1.isPresent());
        assertTrue(result1.get().value() instanceof Match.Character);

        // test *
        Optional<ParseResult<Unit>> result2 = parser.parse("a*");
        assertTrue(result2.isPresent());
        assertTrue(result2.get().value() instanceof QuantifiedExpression);
        QuantifiedExpression quantifiedExpression = (QuantifiedExpression) result2.get().value();
        assertTrue(quantifiedExpression.expression() instanceof Match.Character);
        assertTrue(quantifiedExpression.quantifier().type() instanceof Quantifier.Type.ZeroOrMore);
        assertFalse(quantifiedExpression.quantifier().isLazy());

        // test +
        Optional<ParseResult<Unit>> result3 = parser.parse("a+");
        assertTrue(result3.isPresent());
        assertTrue(result3.get().value() instanceof QuantifiedExpression);
        quantifiedExpression = (QuantifiedExpression) result3.get().value();
        assertTrue(quantifiedExpression.expression() instanceof Match.Character);
        assertTrue(quantifiedExpression.quantifier().type() instanceof Quantifier.Type.OneOrMore);
        assertFalse(quantifiedExpression.quantifier().isLazy());

        // test ?
        Optional<ParseResult<Unit>> result4 = parser.parse("a?");
        assertTrue(result4.isPresent());
        assertTrue(result4.get().value() instanceof QuantifiedExpression);
        quantifiedExpression = (QuantifiedExpression) result4.get().value();
        assertTrue(quantifiedExpression.expression() instanceof Match.Character);
        assertTrue(quantifiedExpression.quantifier().type() instanceof Quantifier.Type.ZeroOrOne);
        assertFalse(quantifiedExpression.quantifier().isLazy());

        // test range {1, 2}
        Optional<ParseResult<Unit>> result5 = parser.parse("a{1,2}");
        assertTrue(result5.isPresent());
        assertTrue(result5.get().value() instanceof QuantifiedExpression);
        quantifiedExpression = (QuantifiedExpression) result5.get().value();
        assertTrue(quantifiedExpression.expression() instanceof Match.Character);
        assertTrue(quantifiedExpression.quantifier().type() instanceof Quantifier.Type.Range);
        assertEquals(1, ((Quantifier.Type.Range) quantifiedExpression.quantifier().type()).lowerBound());
        assertEquals(Optional.of(2), ((Quantifier.Type.Range) quantifiedExpression.quantifier().type()).upperBound());
        assertFalse(quantifiedExpression.quantifier().isLazy());

        // test range {3}
        Optional<ParseResult<Unit>> result6 = parser.parse("a{3}");
        assertTrue(result6.isPresent());
        assertTrue(result6.get().value() instanceof QuantifiedExpression);
        quantifiedExpression = (QuantifiedExpression) result6.get().value();
        assertTrue(quantifiedExpression.expression() instanceof Match.Character);
        assertTrue(quantifiedExpression.quantifier().type() instanceof Quantifier.Type.Range);
        assertEquals(3, ((Quantifier.Type.Range) quantifiedExpression.quantifier().type()).lowerBound());
        assertEquals(Optional.of(3), ((Quantifier.Type.Range) quantifiedExpression.quantifier().type()).upperBound());
        assertFalse(quantifiedExpression.quantifier().isLazy());

        // test range {3,}
        Optional<ParseResult<Unit>> result7 = parser.parse("a{3,}");
        assertTrue(result7.isPresent());
        assertTrue(result7.get().value() instanceof QuantifiedExpression);
        quantifiedExpression = (QuantifiedExpression) result7.get().value();
        assertTrue(quantifiedExpression.expression() instanceof Match.Character);
        assertTrue(quantifiedExpression.quantifier().type() instanceof Quantifier.Type.Range);
        assertEquals(3, ((Quantifier.Type.Range) quantifiedExpression.quantifier().type()).lowerBound());
        assertEquals(Optional.empty(), ((Quantifier.Type.Range) quantifiedExpression.quantifier().type()).upperBound());
        assertFalse(quantifiedExpression.quantifier().isLazy());

        // test lazy
        Optional<ParseResult<Unit>> result8 = parser.parse("a+?");
        assertTrue(result8.isPresent());
        assertTrue(result8.get().value() instanceof QuantifiedExpression);
        quantifiedExpression = (QuantifiedExpression) result8.get().value();
        assertTrue(quantifiedExpression.expression() instanceof Match.Character);
        assertTrue(quantifiedExpression.quantifier().isLazy());

        // test non parsing
        Optional<ParseResult<Unit>> result9 = parser.parse("b");
        assertFalse(result9.isPresent());
    }

    @Test
    public void testEscapedAnchor() {
        Parser<Anchor> parser = Grammer.ESCAPED_ANCHOR;
        Optional<ParseResult<Anchor>> result = parser.parse("\\b");

        // test Word Bondary
        assertTrue(result.isPresent());
        assertEquals(Anchor.WORD_BOUNDARY, result.get().value());

        // test Non-Word Boundary
        result = parser.parse("\\B");
        assertTrue(result.isPresent());
        assertEquals(Anchor.NON_WORD_BOUNDARY, result.get().value());
    }

    @Test
    public void testAnchor() {
        Parser<Anchor> parser = Grammer.ANCHOR;
        Optional<ParseResult<Anchor>> result = parser.parse("$");

        assertTrue(result.isPresent());
        assertEquals(Anchor.END_OF_STRING, result.get().value());
    }

    @Test
    public void testBackReference() {
        Parser<BackReference> parser = Grammer.BACK_REFERENCE;
        Optional<ParseResult<BackReference>> result = parser.parse("\\1");

        assertTrue(result.isPresent());
        assertEquals(1, result.get().value().index());
    }

    @Test
    public void testEscapedCharacter() {
        Parser<Character> parser = Grammer.ESCAPED_CHARACTER;
        Optional<ParseResult<Character>> result = parser.parse("\\n");

        assertTrue(result.isPresent());
        assertEquals('n', result.get().value());
    }

    @Test
    public void testRangeQuantifier() {
        Parser<Quantifier.Type.Range> parser = Grammer.RANGE_QUANTIFIER;
        Optional<ParseResult<Quantifier.Type.Range>> result = parser.parse("{2,3}");

        assertTrue(result.isPresent());
        Quantifier.Type.Range range = result.get().value();
        assertEquals(2, range.lowerBound());
        assertEquals(Optional.of(3), range.upperBound());
    }

    @Test
    public void testCharacterClass() {
        Parser<CharacterSet> parser = Grammer.CHARACTER_CLASS;
        Optional<ParseResult<CharacterSet>> result = parser.parse("\\d");

        assertTrue(result.isPresent());
        assertTrue(result.get().value().contains('0'));
        assertTrue(result.get().value().contains('9'));
    }

    @Test
    public void testCharacterGroup() {
        Parser<CharacterGroup> parser = Grammer.CHARACTER_GROUP;
        Optional<ParseResult<CharacterGroup>> result = parser.parse("[a-z]");

        assertTrue(result.isPresent());
        CharacterGroup group = result.get().value();
        assertFalse(group.isInverted());
        assertEquals(1, group.items().size());
        assertTrue(group.items().get(0) instanceof CharacterGroup.Item.Range);
    }

    @Test
    public void testMatch() {
        Parser<Match> parser = Grammer.MATCH;
        Optional<ParseResult<Match>> result = parser.parse(".");

        assertTrue(result.isPresent());
        assertTrue(result.get().value() instanceof Match.AnyCharacter);
    }

    @Test
    public void testExpression() {
        Parser<Unit> parser = Grammer.EXPRESSION;
        Optional<ParseResult<Unit>> result = parser.parse("a|b");

        assertTrue(result.isPresent());
        assertTrue(result.get().value() instanceof Alternation);
    }

    @Test
    public void testGroup() {
        Parser<Unit> parser = Grammer.GROUP;
        Optional<ParseResult<Unit>> result = parser.parse("(a)");

        assertTrue(result.isPresent());
        assertTrue(result.get().value() instanceof Group);
    }

    @Test
    public void testRegex() {
        Parser<AST> parser = Grammer.REGEX;
        Optional<ParseResult<AST>> result = parser.parse("^a$");

        assertTrue(result.isPresent());
        AST ast = result.get().value();
        assertTrue(ast.isFromStartOfString());
        assertTrue(ast.root() instanceof ImplicitGroup);
    }
}