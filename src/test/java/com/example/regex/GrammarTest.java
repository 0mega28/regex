
package com.example.regex;

import static com.example.regex.Grammar.CHARACTER_CLASS_FROM_UNICODE_CATEGORY;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Optional;

public class GrammarTest {

    @Test
    public void testQuantified() {
        Parser<Unit> parser = Grammar.quantified(Parsers.string("a")
                .map(value -> new Match.Character('a')));

        Optional<ParseResult<Unit>> result1 = parser.parse("a");
        assertTrue(result1.isPresent());
        assertInstanceOf(Match.Character.class, result1.get().value());

        // test *
        Optional<ParseResult<Unit>> result2 = parser.parse("a*");
        assertTrue(result2.isPresent());
        assertInstanceOf(QuantifiedExpression.class, result2.get().value());
        QuantifiedExpression quantifiedExpression = (QuantifiedExpression) result2.get().value();
        assertInstanceOf(Match.Character.class, quantifiedExpression.expression());
        assertInstanceOf(Quantifier.Type.ZeroOrMore.class, quantifiedExpression.quantifier().type());
        assertFalse(quantifiedExpression.quantifier().isLazy());

        // test +
        Optional<ParseResult<Unit>> result3 = parser.parse("a+");
        assertTrue(result3.isPresent());
        assertInstanceOf(QuantifiedExpression.class, result3.get().value());
        quantifiedExpression = (QuantifiedExpression) result3.get().value();
        assertInstanceOf(Match.Character.class, quantifiedExpression.expression());
        assertInstanceOf(Quantifier.Type.OneOrMore.class, quantifiedExpression.quantifier().type());
        assertFalse(quantifiedExpression.quantifier().isLazy());

        // test ?
        Optional<ParseResult<Unit>> result4 = parser.parse("a?");
        assertTrue(result4.isPresent());
        assertInstanceOf(QuantifiedExpression.class, result4.get().value());
        quantifiedExpression = (QuantifiedExpression) result4.get().value();
        assertInstanceOf(Match.Character.class, quantifiedExpression.expression());
        assertInstanceOf(Quantifier.Type.ZeroOrOne.class, quantifiedExpression.quantifier().type());
        assertFalse(quantifiedExpression.quantifier().isLazy());

        // test range {1, 2}
        Optional<ParseResult<Unit>> result5 = parser.parse("a{1,2}");
        assertTrue(result5.isPresent());
        assertInstanceOf(QuantifiedExpression.class, result5.get().value());
        quantifiedExpression = (QuantifiedExpression) result5.get().value();
        assertInstanceOf(Match.Character.class, quantifiedExpression.expression());
        assertInstanceOf(Quantifier.Type.Range.class, quantifiedExpression.quantifier().type());
        assertEquals(1, ((Quantifier.Type.Range) quantifiedExpression.quantifier().type()).lowerBound());
        assertEquals(Optional.of(2), ((Quantifier.Type.Range) quantifiedExpression.quantifier().type()).upperBound());
        assertFalse(quantifiedExpression.quantifier().isLazy());

        // test range {3}
        Optional<ParseResult<Unit>> result6 = parser.parse("a{3}");
        assertTrue(result6.isPresent());
        assertInstanceOf(QuantifiedExpression.class, result6.get().value());
        quantifiedExpression = (QuantifiedExpression) result6.get().value();
        assertInstanceOf(Match.Character.class, quantifiedExpression.expression());
        assertInstanceOf(Quantifier.Type.Range.class, quantifiedExpression.quantifier().type());
        assertEquals(3, ((Quantifier.Type.Range) quantifiedExpression.quantifier().type()).lowerBound());
        assertEquals(Optional.of(3), ((Quantifier.Type.Range) quantifiedExpression.quantifier().type()).upperBound());
        assertFalse(quantifiedExpression.quantifier().isLazy());

        // test range {3,}
        Optional<ParseResult<Unit>> result7 = parser.parse("a{3,}");
        assertTrue(result7.isPresent());
        assertInstanceOf(QuantifiedExpression.class, result7.get().value());
        quantifiedExpression = (QuantifiedExpression) result7.get().value();
        assertInstanceOf(Match.Character.class, quantifiedExpression.expression());
        assertInstanceOf(Quantifier.Type.Range.class, quantifiedExpression.quantifier().type());
        assertEquals(3, ((Quantifier.Type.Range) quantifiedExpression.quantifier().type()).lowerBound());
        assertEquals(Optional.empty(), ((Quantifier.Type.Range) quantifiedExpression.quantifier().type()).upperBound());
        assertFalse(quantifiedExpression.quantifier().isLazy());

        // test lazy
        Optional<ParseResult<Unit>> result8 = parser.parse("a+?");
        assertTrue(result8.isPresent());
        assertInstanceOf(QuantifiedExpression.class, result8.get().value());
        quantifiedExpression = (QuantifiedExpression) result8.get().value();
        assertInstanceOf(Match.Character.class, quantifiedExpression.expression());
        assertTrue(quantifiedExpression.quantifier().isLazy());
        // test non parsing
        Optional<ParseResult<Unit>> result9 = parser.parse("b");
        assertFalse(result9.isPresent());
    }

    @Test
    public void testAnchor() {
        Parser<Anchor> parser = Grammar.ANCHOR;
        Optional<ParseResult<Anchor>> result = parser.parse("\\b");

        // test Word Boundary
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
        Parser<BackReference> parser = Grammar.BACK_REFERENCE;
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
        Parser<Character> parser = Grammar.ESCAPED_CHARACTER;
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
        Parser<Quantifier.Type.Range> parser = Grammar.RANGE_QUANTIFIER;
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
        Parser<CharacterSet> parser = Grammar.CHARACTER_CLASS;
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
        Parser<CharacterGroup.Item.Range> parser = Grammar.CHARACTER_RANGE;
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

        parser = Grammar.CHARACTER_GROUP_ITEM;
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
        Parser<CharacterGroup> parser = Grammar.CHARACTER_GROUP;
        Optional<ParseResult<CharacterGroup>> result = parser.parse("[a-z]");

        assertTrue(result.isPresent());
        CharacterGroup group = result.get().value();
        assertFalse(group.isInverted());
        assertEquals(1, group.items().size());
        assertInstanceOf(CharacterGroup.Item.Range.class, group.items().getFirst());

        result = parser.parse("[^a-z]");
        assertTrue(result.isPresent());
        group = result.get().value();
        assertTrue(group.isInverted());
        assertEquals(1, group.items().size());
        assertInstanceOf(CharacterGroup.Item.Range.class, group.items().getFirst());

        result = parser.parse("[^abc-d\\d\\a]");
        assertTrue(result.isPresent());
        group = result.get().value();
        assertTrue(group.isInverted());
        assertEquals(5, group.items().size());
        assertInstanceOf(CharacterGroup.Item.Character.class, group.items().get(0));
        assertInstanceOf(CharacterGroup.Item.Character.class, group.items().get(1));
        assertInstanceOf(CharacterGroup.Item.Range.class, group.items().get(2));
        assertInstanceOf(CharacterGroup.Item.Set.class, group.items().get(3));
        assertInstanceOf(CharacterGroup.Item.Character.class, group.items().get(4));

        assertEquals('a', ((CharacterGroup.Item.Character) group.items().get(0)).character());
        assertEquals('b', ((CharacterGroup.Item.Character) group.items().get(1)).character());
        var range = (CharacterGroup.Item.Range) group.items().get(2);
        assertEquals('c', range.start());
        assertEquals('d', range.end());
        var set = (CharacterGroup.Item.Set) group.items().get(3);
        assertEquals(CharacterSet.decimalDigit, set.set());
        var escapedCharacter = (CharacterGroup.Item.Character) group.items().get(4);
        assertEquals('a', escapedCharacter.character());

        assertThrows(ParseException.class, () -> parser.parse("[]"));
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
        Parser<Match> parser = Grammar.MATCH;
        Optional<ParseResult<Match>> result = parser.parse(".");

        assertTrue(result.isPresent());
        assertTrue(result.get().value() instanceof Match.AnyCharacter);
    }

    @Test
    public void testExpression() {
        Parser<Unit> parser = Grammar.EXPRESSION;
        Optional<ParseResult<Unit>> result = parser.parse("a|b");

        assertTrue(result.isPresent());
        assertTrue(result.get().value() instanceof Alternation);
    }

    @Test
    public void testGroup() {
        Parser<Unit> parser = Grammar.GROUP;
        Optional<ParseResult<Unit>> result = parser.parse("(a)");

        assertTrue(result.isPresent());
        assertTrue(result.get().value() instanceof Group);
    }

    @Test
    public void testRegex() {
        Parser<AST> parser = Grammar.REGEX;
        Optional<ParseResult<AST>> result = parser.parse("^a$");

        assertTrue(result.isPresent());
        AST ast = result.get().value();
        assertTrue(ast.isFromStartOfString());
        assertTrue(ast.root() instanceof ImplicitGroup);
    }
}