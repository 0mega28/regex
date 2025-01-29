package com.example.regex;

@FunctionalInterface
interface CharacterSet {

    boolean contains(char character);

    static CharacterSet charactersIn(String allowed) {
        return character -> allowed.indexOf(character) != -1;
    }

    default CharacterSet union(CharacterSet other) {
        return character -> CharacterSet.this.contains(character) || other.contains(character);
    }

    default CharacterSet inverted() {
        return character -> !CharacterSet.this.contains(character);
    }

    // Common predefined sets as static methods
    static CharacterSet decimalDigit = character -> Character.isDigit(character);

    static CharacterSet whiteSpaces = character -> Character.isWhitespace(character);

    static CharacterSet alphaNumeric = character -> Character.isLetterOrDigit(character);

    static CharacterSet word = alphaNumeric.union(charactersIn("_"));

    static CharacterSet punctuationCharacters = character -> {
        var type = Character.getType(character);
        return type == Character.CONNECTOR_PUNCTUATION ||
                type == Character.DASH_PUNCTUATION ||
                type == Character.START_PUNCTUATION ||
                type == Character.END_PUNCTUATION ||
                type == Character.OTHER_PUNCTUATION ||
                type == Character.INITIAL_QUOTE_PUNCTUATION ||
                type == Character.FINAL_QUOTE_PUNCTUATION;
    };

    static CharacterSet capitalizedLetters = character -> Character.isUpperCase(character);

    static CharacterSet lowerCaseCharacters = character -> Character.isLowerCase(character);

    static CharacterSet nonBaseCharacters = character -> Character.getType(character) == Character.NON_SPACING_MARK;

    static CharacterSet symbols = character -> {
        var type = Character.getType(character);
        return type == Character.MATH_SYMBOL ||
                type == Character.CURRENCY_SYMBOL ||
                type == Character.MODIFIER_SYMBOL ||
                type == Character.OTHER_SYMBOL;
    };
}
