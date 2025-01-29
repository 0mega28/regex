
package com.example.regex;

import static com.example.regex.Grammer.CHARACTER_CLASS_FROM_UNICODE_CATEGORY;
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
    public void testAnchor() {
        Parser<Anchor> parser = Grammer.ANCHOR;
        Optional<ParseResult<Anchor>> result = parser.parse("\\b");

        // test Word Bondary
        assertTrue(result.isPresent());
        assertEquals(Anchor.WORD_BOUNDARY, result.get().value());

        // test Non-Word Boundary
        result = parser.parse("\\B");
        assertTrue(result.isPresent());
        assertEquals(Anchor.NON_WORD_BOUNDARY, result.get().value());

        // test Start of String Only
        result = parser.parse("\\A");
        assertTrue(result.isPresent());
        assertEquals(Anchor.START_OF_STRING_ONLY, result.get().value());

        // test End of String Only
        result = parser.parse("\\Z");
        assertTrue(result.isPresent());
        assertEquals(Anchor.END_OF_STRING_ONLY, result.get().value());

        // test End of String Only Not Newline
        result = parser.parse("\\z");
        assertTrue(result.isPresent());
        assertEquals(Anchor.END_OF_STRING_ONLY_NOT_NEWLINE, result.get().value());

        // test Previous Match End
        result = parser.parse("\\G");
        assertTrue(result.isPresent());
        assertEquals(Anchor.PREVIOUS_MATCH_END, result.get().value());

        result = parser.parse("$");
        assertTrue(result.isPresent());
        assertEquals(Anchor.END_OF_STRING, result.get().value());

        // test invalid
        assertFalse(parser.parse("\\c").isPresent());
        assertFalse(parser.parse("\\").isPresent());
        assertFalse(parser.parse("a").isPresent());
    }

    @Test
    public void testBackReference() {
        Parser<BackReference> parser = Grammer.BACK_REFERENCE;
        Optional<ParseResult<BackReference>> result = parser.parse("\\1");

        assertTrue(result.isPresent());
        assertEquals(1, result.get().value().index());

        result = parser.parse("\\10");
        assertTrue(result.isPresent());
        assertEquals(10, result.get().value().index());

        // test invalid
        assertFalse(parser.parse("\\").isPresent());
        assertFalse(parser.parse("\0").isPresent());
        assertFalse(parser.parse("").isPresent());
        assertFalse(parser.parse("\\a").isPresent());
    }

    @Test
    public void testEscapedCharacter() {
        Parser<Character> parser = Grammer.ESCAPED_CHARACTER;
        Optional<ParseResult<Character>> result = parser.parse("\\n");

        assertTrue(result.isPresent());
        assertEquals('n', result.get().value());

        result = parser.parse("\\t");
        assertTrue(result.isPresent());
        assertEquals('t', result.get().value());

        result = parser.parse("\\1");
        assertTrue(result.isPresent());
        assertEquals('1', result.get().value());

        // failed
        assertFalse(parser.parse("a").isPresent());
        assertFalse(parser.parse("/").isPresent());
        assertThrows(ParseException.class, () -> parser.parse("\\").isPresent());
    }

    @Test
    public void testRangeQuantifier() {
        Parser<Quantifier.Type.Range> parser = Grammer.RANGE_QUANTIFIER;
        Optional<ParseResult<Quantifier.Type.Range>> result = parser.parse("{2,3}");

        assertTrue(result.isPresent());
        Quantifier.Type.Range range = result.get().value();
        assertEquals(2, range.lowerBound());
        assertEquals(Optional.of(3), range.upperBound());

        result = parser.parse("{2,}");
        assertTrue(result.isPresent());
        range = result.get().value();
        assertEquals(2, range.lowerBound());
        assertEquals(Optional.empty(), range.upperBound());

        result = parser.parse("{3}");
        assertTrue(result.isPresent());
        range = result.get().value();
        assertEquals(3, range.lowerBound());
        assertEquals(Optional.of(3), range.upperBound());

        // test failed
        assertFalse(parser.parse("{,3}").isPresent());
        assertFalse(parser.parse("{3").isPresent());
        assertFalse(parser.parse("{3,").isPresent());
        assertFalse(parser.parse("{}").isPresent());
        assertFalse(parser.parse("}").isPresent());
        assertFalse(parser.parse("{").isPresent());
        assertFalse(parser.parse("").isPresent());
    }

    @Test
    public void testCharacterClass() {
        Parser<CharacterSet> parser = Grammer.CHARACTER_CLASS;
        Optional<ParseResult<CharacterSet>> result = parser.parse("\\d");

        assertTrue(result.isPresent());
        assertEquals(CharacterSet.decimalDigit, result.get().value());

        result = parser.parse("\\D");
        assertTrue(result.isPresent());
        assertTrue(result.get().value().contains('a'));
        assertFalse(result.get().value().contains('1'));

        result = parser.parse("\\w");
        assertTrue(result.isPresent());
        assertEquals(CharacterSet.word, result.get().value());

        result = parser.parse("\\W");
        assertTrue(result.isPresent());
        assertFalse(result.get().value().contains('a'));
        assertFalse(result.get().value().contains('1'));

        result = parser.parse("\\s");
        assertTrue(result.isPresent());
        assertEquals(CharacterSet.whiteSpaces, result.get().value());

        result = parser.parse("\\S");
        assertTrue(result.isPresent());
        assertTrue(result.get().value().contains('a'));
        assertTrue(result.get().value().contains('1'));
        assertFalse(result.get().value().contains(' '));
        assertFalse(result.get().value().contains('\t'));

        // invalid
        assertTrue(parser.parse("\\p").isEmpty());
        assertTrue(parser.parse("\\P").isEmpty());
        assertTrue(parser.parse("\\").isEmpty());
        assertTrue(parser.parse("a").isEmpty());
        assertTrue(parser.parse("").isEmpty());
    }

    @Test
    public void testCharacterRange() {
        Parser<CharacterGroup.Item.Range> parser = Grammer.CHARACTER_RANGE;
        Optional<ParseResult<CharacterGroup.Item.Range>> result = parser.parse("a-z");
        assertTrue(result.isPresent());
        var range = result.get().value();
        assertEquals('a', range.start());
        assertEquals('z', range.end());

        assertTrue(parser.parse("a-").isEmpty());
        assertTrue(parser.parse("-z").isEmpty());
        assertTrue(parser.parse("[a-b]").isEmpty());
        assertTrue(parser.parse("[a-b").isEmpty());
        assertTrue(parser.parse("[]").isEmpty());
        assertTrue(parser.parse("-").isEmpty());
        assertTrue(parser.parse("").isEmpty());
    }

    @Test
    public void testCharacterGroupItem() {
        Parser<CharacterGroup.Item> parser;
        Optional<ParseResult<CharacterGroup.Item>> result;

        parser = Grammer.CHARACTER_GROUP_ITEM;
        result = parser.parse("a");
        assertTrue(result.isPresent());
        assertInstanceOf(CharacterGroup.Item.Character.class, result.get().value());
        CharacterGroup.Item.Character character = (CharacterGroup.Item.Character) result.get().value();
        assertEquals('a', character.character());

        result = parser.parse("-");
        assertTrue(result.isPresent());
        assertInstanceOf(CharacterGroup.Item.Character.class, result.get().value());
        character = (CharacterGroup.Item.Character) result.get().value();
        assertEquals('-', character.character());
    }


    @Test
    public void testCharacterGroup() {
        Parser<CharacterGroup> parser = Grammer.CHARACTER_GROUP;
        Optional<ParseResult<CharacterGroup>> result = parser.parse("[a-z]");

        assertTrue(result.isPresent());
        CharacterGroup group = result.get().value();
        assertFalse(group.isInverted());
        assertEquals(1, group.items().size());
        assertInstanceOf(CharacterGroup.Item.Range.class, group.items().getFirst());
    }

    @Test
    void testValidUnicodeCategories() throws ParseException {
        Parser<CharacterSet> parser = CHARACTER_CLASS_FROM_UNICODE_CATEGORY;
        assertEquals(CharacterSet.punctuationCharacters, parser.parse("\\p{P}").map(ParseResult::value).orElseThrow());
        assertEquals(CharacterSet.capitalizedLetters, parser.parse("\\p{Lt}").map(ParseResult::value).orElseThrow());
        assertEquals(CharacterSet.lowerCaseCharacters, parser.parse("\\p{Ll}").map(ParseResult::value).orElseThrow());
        assertEquals(CharacterSet.nonBaseCharacters, parser.parse("\\p{N}").map(ParseResult::value).orElseThrow());
        assertEquals(CharacterSet.symbols, parser.parse("\\p{S}").map(ParseResult::value).orElseThrow());

        // test inverted
        CharacterSet set = parser.parse("\\P{P}").orElseThrow().value();
        assertFalse(set.contains('.'));
        assertFalse(set.contains('!'));
        assertTrue(set.contains('A')); // A non-punctuation character

        set = parser.parse("\\P{Lt}").orElseThrow().value();
        assertTrue(set.contains('b')); // Lowercase letter

        set = parser.parse("\\P{Ll}").orElseThrow().value();
        assertFalse(set.contains('a'));
        assertTrue(set.contains('A')); // Uppercase letter

        set = parser.parse("\\P{N}").orElseThrow().value();
        assertTrue(set.contains('1'));
        assertTrue(set.contains('A')); // Non-numeric character

        set = parser.parse("\\P{S}").orElseThrow().value();
        assertFalse(set.contains('+'));
        assertTrue(set.contains('b')); // A non-symbol character

        // test invalid
        assertThrows(ParseException.class, () -> parser.parse("\\p{XYZ}"));
        assertThrows(ParseException.class, () -> parser.parse("\\pP}"));
        assertThrows(ParseException.class, () -> parser.parse("\\p{P"));
        assertThrows(ParseException.class, () -> parser.parse("\\p{}"));
        assertFalse(parser.parse("\\q{P}").isPresent()); // 'q' is invalid
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