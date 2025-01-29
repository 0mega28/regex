package com.example.regex;

import static com.example.regex.Parsers.*;
import static com.example.regex.ParserCombinators.*;

import java.util.List;
import java.util.Optional;

interface Grammer {
        String QUANTIFIERS = "*+?";

        static Parser<Unit> quantified(Parser<Unit> parser) {
                return zip(
                                parser,
                                optional(QUANTIFIER))
                                .map(result -> {
                                        Unit expression = result.firstValue();
                                        Optional<Quantifier> quantifierOpt = result.secondValue();

                                        return quantifierOpt
                                                        .map(quantifier -> (Unit) new QuantifiedExpression(expression,
                                                                        quantifier))
                                                        .orElse(expression);
                                });
        }

        Parser<Void> END_OF_PATTERN = oneOf(
                        end(),
                        string(")").zeroOrThrow("Unmatched closing parenthesis"));

        Parser<Anchor> ESCAPED_ANCHOR = second(
                        string("\\"),
                        charFrom("bBAZzG"))
                        .map(character -> (switch (character) {
                                case 'b' -> Anchor.WORD_BOUNDARY;
                                case 'B' -> Anchor.NON_WORD_BOUNDARY;
                                case 'A' -> Anchor.START_OF_STRING_ONLY;
                                case 'Z' -> Anchor.END_OF_STRING_ONLY;
                                case 'z' -> Anchor.END_OF_STRING_ONLY_NOT_NEWLINE;
                                case 'G' -> Anchor.PREVIOUS_MATCH_END;
                                default -> throw new IllegalStateException("Unexpected value: " + character);
                        }));

        Parser<Anchor> ANCHOR = oneOf(
                        ESCAPED_ANCHOR,
                        string("$").map(value -> Anchor.END_OF_STRING));

        Parser<BackReference> BACK_REFERENCE = second(
                        string("\\"),
                        number())
                        .map(BackReference::new);

        Parser<Character> ESCAPED_CHARACTER = second(
                        string("\\"),
                        charParser().orThrow("Pattern may not end with a trailing backslash"));

        Parser<Quantifier.Type.Range> RANGE_QUANTIFIER = zip(
                        second(
                                        string("{"),
                                        number()),
                        first(
                                        optional(
                                                        second(
                                                                        string(","),
                                                                        optional(number()))),
                                        string("}")))
                        .map(result -> {
                                Integer lhs = result.firstValue();
                                Optional<Optional<Integer>> rhs = result.secondValue();

                                return new Quantifier.Type.Range(lhs, rhs.orElse(Optional.of(lhs)));
                        });

        Parser<Quantifier.Type> QUANTIFIER_TYPE = oneOf(
                        string("?").map(value -> new Quantifier.Type.ZeroOrOne()),
                        string("*").map(value -> new Quantifier.Type.ZeroOrMore()),
                        string("+").map(value -> new Quantifier.Type.OneOrMore()),
                        RANGE_QUANTIFIER.map(value -> value));

        Parser<Quantifier> QUANTIFIER = zip(
                        QUANTIFIER_TYPE,
                        optionalb(string("?")))
                        .map(result -> new Quantifier(result.firstValue(), result.secondValue()));

        Parser<CharacterSet> UNICODE_CATEGORY = first(
                        second(
                                        string("{").orThrow("Missing unicode category name"),
                                        stringExcluding("}").orThrow("Missing unicode category name")),
                        string("}").orThrow("Missing closing brace"))
                        .map(Util::characterListToString)
                        .map(name -> (switch (name) {
                                case "P" -> CharacterSet.punctuationCharacters;
                                case "Lt" -> CharacterSet.capitalizedLetters;
                                case "Ll" -> CharacterSet.lowerCaseCharacters;
                                case "N" -> CharacterSet.nonBaseCharacters;
                                case "S" -> CharacterSet.symbols;
                                default -> throw new ParseException("Unknown unicode category: " + name);
                        }));

        // A unicode category, e.g. "\p{P}" - all punctuation characters.
        Parser<CharacterSet> CHARACTER_CLASS_FROM_UNICODE_CATEGORY = zip(
                        second(
                                        string("\\"),
                                        charFrom("pP")),
                        UNICODE_CATEGORY)
                        .map(result -> {
                                char type = result.firstValue();
                                CharacterSet set = result.secondValue();

                                return type == 'p' ? set : set.inverted();
                        });

        Parser<CharacterSet> CHARACTER_CLASS = second(
                        string("\\"),
                        charFrom("dDsSwW"))
                        .map(chr -> (switch (chr) {
                                case 'd' -> CharacterSet.decimalDigit;
                                case 'D' -> CharacterSet.decimalDigit.inverted();
                                case 's' -> CharacterSet.whiteSpaces;
                                case 'S' -> CharacterSet.whiteSpaces.inverted();
                                case 'w' -> CharacterSet.word;
                                case 'W' -> CharacterSet.word.inverted();
                                default -> throw new IllegalStateException("Unexpected value: " + chr);
                        }));

        Parser<CharacterSet> CHARACTER_SET = oneOf(CHARACTER_CLASS,
                        CHARACTER_CLASS_FROM_UNICODE_CATEGORY);

        // Character range e.g. a-z
        Parser<CharacterGroup.Item.Range> CHARACTER_RANGE = zip(
                        first(
                                        charExcluding("]"),
                                        string("-")),
                        charExcluding("]"))
                        .map(result -> new CharacterGroup.Item.Range(result.firstValue(), result.secondValue()));

        Parser<CharacterGroup.Item> CHARACTER_GROUP_ITEM = oneOf(
                        string("/").zeroOrThrow("An unescaped delimeter must be escaped with a backslash")
                                        .map(value -> null),
                        CHARACTER_SET.map(CharacterGroup.Item.Set::new).map(value -> (CharacterGroup.Item) value),
                        CHARACTER_RANGE.map(value -> value),
                        ESCAPED_CHARACTER.map(CharacterGroup.Item.Character::new)
                                        .map(value -> value),
                        charExcluding("]").map(CharacterGroup.Item.Character::new)
                                        .map(value -> value));

        Parser<CharacterGroup> CHARACTER_GROUP = zip(
                        second(
                                        string("["),
                                        optional(string("^"))),
                        first(
                                        CHARACTER_GROUP_ITEM.oneOrMore().orThrow("Character group is empty"),
                                        string("]").orThrow("Character group missing closing square bracket")))
                        .map(result -> new CharacterGroup(result.firstValue().isPresent(), result.secondValue()));

        // Any subexpression that is used for matching against the input string, e.g.
        // "a" - matches a character, "[a-z]" – matches a character group, etc.
        Parser<Match> MATCH = oneOf(
                        string(".").map(value -> new Match.AnyCharacter()),
                        CHARACTER_GROUP.map(Match.Group::new),
                        CHARACTER_SET.map(Match.Set::new),
                        ESCAPED_CHARACTER.map(Match.Character::new),
                        charExcluding(")|" + QUANTIFIERS).map(Match.Character::new));

        Parser<Unit> SUB_EXPRESSION = oneOf(
                        quantified(lazy(() -> Grammer.GROUP).map(value -> (Unit) value)),
                        ANCHOR.map(value -> (Unit) value),
                        BACK_REFERENCE.map(value -> (Unit) value),
                        quantified(MATCH.map(value -> (Unit) value)),
                        charFrom(QUANTIFIERS).zeroOrThrow("The preceeding token is not quantifiable")
                                        .map(value -> (Unit) null))
                        .oneOrMore()
                        .orThrow("Pattern must not be empty")
                        .map(Grammer::flatten);

        Parser<Unit> EXPRESSION = zip(
                        SUB_EXPRESSION,
                        optional(
                                        second(string("|"),
                                                        lazy(() -> Grammer.EXPRESSION))))
                        .map(result -> {
                                Unit lhs = result.firstValue();
                                Optional<Unit> rhs = result.secondValue();

                                return rhs.map(value -> (Unit) new Alternation(List.of(lhs, value)))
                                                .orElse(lhs);
                        });

        Parser<Unit> GROUP = zip(
                        second(
                                        string("("),
                                        optional(string("?:"))),
                        first(
                                        EXPRESSION,
                                        string(")").orThrow("Unmatched opening parenthesis")))
                        .map(result -> {
                                boolean isCapturing = result.firstValue().isPresent();
                                return new Group(Optional.empty(), isCapturing, List.of(result.secondValue()));
                        });

        Parser<AST> REGEX = zip(
                        optional(string("^")),
                        first(Grammer.EXPRESSION, Grammer.END_OF_PATTERN))
                        .map(result -> new AST(result.firstValue().isPresent(),
                                        result.secondValue()));

        static Unit flatten(List<Unit> children) {
                if (children.isEmpty())
                        throw new IllegalStateException("Unexpected empty list of children");

                return children.size() == 1
                                ? children.getFirst()
                                : new ImplicitGroup(children);
        }

}
