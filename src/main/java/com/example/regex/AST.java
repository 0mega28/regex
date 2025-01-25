package com.example.regex;

import java.util.List;

interface ASTNode {
}

record GroupNode(ASTNode child) implements ASTNode {
}

record Expression(List<ASTNode> children) implements ASTNode {
    Expression {
        children = List.copyOf(children);
    }

    public List<ASTNode> children() {
        return List.copyOf(this.children);
    }

}

record StringNode(String value) implements ASTNode {
}

record Alternation(List<ASTNode> options) implements ASTNode {
    Alternation {
        options = List.copyOf(options);
    }

    public List<ASTNode> options() {
        return List.copyOf(this.options);
    }
}
