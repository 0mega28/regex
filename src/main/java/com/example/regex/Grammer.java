package com.example.regex;

import static com.example.regex.Parsers.*;
import static com.example.regex.ParserCombinators.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Grammer {
    static final Parser<ASTNode> parser = lazy(() -> Grammer.expressionParser).map(ASTNode.class::cast)
            .flatMap(result -> Parsers.end().map(end -> result));

    static final Parser<StringNode> stringParser = stringExcluding("()|*?+[]{}\\")
            .map(characterList -> new StringNode(
                    characterList.stream().map(String::valueOf).collect(Collectors.joining())));

    static final Parser<Alternation> alternationParser = zip(
            stringParser,
            chooseSecond(Parsers.string("|"), stringParser).oneOrMore())
            .map(result -> {
                List<ASTNode> options = new ArrayList<>();
                options.add(result.firstValue());
                options.addAll(result.secondValue());
                return new Alternation(options);
            });

    static final Parser<Expression> expressionParser = oneOf(
            alternationParser.map(ASTNode.class::cast),
            stringParser.map(ASTNode.class::cast),
            lazy(() -> Grammer.groupParser).map(ASTNode.class::cast))
            .oneOrMore()
            .map(Expression::new);

    static final Parser<GroupNode> groupParser = zip(
            string("("),
            expressionParser,
            string(")").orThrow("Expected closing parenthesis")).map(result -> new GroupNode(result.secondValue()));
}
