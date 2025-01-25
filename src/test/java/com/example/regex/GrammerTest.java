package com.example.regex;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class GrammerTest {

    @Test
    void testStringParser() throws Exception {
        String input = "hello";
        Optional<ParseResult<StringNode>> result = Grammer.stringParser.parse(input);

        assertTrue(result.isPresent(), "String parser should match the input");
        assertEquals("hello", (result.get().value()).value(), "Parsed value should match input string");
        assertEquals("", result.get().remaining(), "Remaining input should be empty after parsing");

        // not fully parseable basically contains some reserved regex character
        String input2 = "he(llo)";
        Optional<ParseResult<StringNode>> result2 = Grammer.stringParser.parse(input2);
        assertTrue(result2.isPresent(), "String parser should match the input");
        assertEquals("he", (result2.get().value()).value(), "Parsed value should match input string");
        assertEquals("(llo)", result2.get().remaining(), "Remaining input should be empty after parsing");
    }

    @Test
    void testAlternationParser() throws Exception {
        String input = "red|blue|green";
        Optional<ParseResult<Alternation>> result = Grammer.alternationParser.parse(input);

        assertTrue(result.isPresent(), "Alternation parser should match the input");
        Alternation alternation = result.get().value();

        assertEquals(3, alternation.options().size(), "Alternation should have two options");
        assertEquals("red", ((StringNode) alternation.options().get(0)).value(), "First option should match");
        assertEquals("blue", ((StringNode) alternation.options().get(1)).value(), "Second option should match");
        assertEquals("green", ((StringNode) alternation.options().get(2)).value(), "Second option should match");
        assertEquals("", result.get().remaining(), "Remaining input should be empty after parsing");
    }

    @Test
    void testGroupParser() throws Exception {
        String input = "(hello|world)";
        Optional<ParseResult<GroupNode>> result = Grammer.groupParser.parse(input);

        assertTrue(result.isPresent(), "Group parser should match the input");
        GroupNode group = result.get().value();

        assertTrue(group.child() instanceof Expression, "Group child should be an Expression");
        Expression expression = (Expression) group.child();

        assertEquals(1, expression.children().size(), "Expression should have two children");
        Alternation alternation = (Alternation) expression.children().get(0);
        assertEquals("hello", ((StringNode) alternation.options().get(0)).value(), "First option should match");
        assertEquals("world", ((StringNode) alternation.options().get(1)).value(), "Second option should match");
        assertEquals("", result.get().remaining(), "Remaining input should be empty after parsing");
    }

    @Test
    void testExpressionParser() throws Exception {
        String input = "the (red|blue) pill";
        Optional<ParseResult<ASTNode>> result = Grammer.parser.parse(input);

        assertTrue(result.isPresent(), "Expression parser should match the input");
        Expression expression = (Expression) result.get().value();

        assertEquals(3, expression.children().size(), "Expression should have three children");
        assertEquals("the ", ((StringNode) expression.children().get(0)).value(), "First child should be 'the '");

        GroupNode group = (GroupNode) expression.children().get(1);
        assertTrue(group.child() instanceof Expression, "Second child should be a Group containing an Expression");

        Alternation alternation = (Alternation) ((Expression) group.child()).children().get(0);
        assertEquals("red", ((StringNode) alternation.options().get(0)).value(),
                "First option in alternation should match");
        assertEquals("blue", ((StringNode) alternation.options().get(1)).value(),
                "Second option in alternation should match");

        assertEquals(" pill", ((StringNode) expression.children().get(2)).value(), "Third child should be ' pill'");
        assertEquals("", result.get().remaining(), "Remaining input should be empty after parsing");
    }

    @Test
    void testInvalidInput() throws Exception {
        assertThrows(ParseException.class, () -> Grammer.parser.parse("the (red|blue"));
    }
}
