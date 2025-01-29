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
    static CharacterSet decimalDigit() {
        return character -> Character.isDigit(character);
    }

    static CharacterSet whiteSpaces() {
        return character -> Character.isWhitespace(character);
    }

    static CharacterSet alphaNumeric() {
        return character -> Character.isLetterOrDigit(character);
    }

    static CharacterSet word() {
        return alphaNumeric().union(charactersIn("_"));
    }

    static CharacterSet punctuationCharacters() {
        return character -> {
            var type = Character.getType(character);
            return type == Character.CONNECTOR_PUNCTUATION ||
                    type == Character.DASH_PUNCTUATION ||
                    type == Character.START_PUNCTUATION ||
                    type == Character.END_PUNCTUATION ||
                    type == Character.OTHER_PUNCTUATION ||
                    type == Character.INITIAL_QUOTE_PUNCTUATION ||
                    type == Character.FINAL_QUOTE_PUNCTUATION;
        };
    }

    static CharacterSet capitalizedLetters() {
        return character -> Character.isUpperCase(character);
    }

    static CharacterSet lowerCaseCharacters() {
        return character -> Character.isLowerCase(character);
    }

    static CharacterSet nonBaseCharacters() {
        return character -> Character.getType(character) == Character.NON_SPACING_MARK;
    }

    static CharacterSet symbols() {
        return character -> {
            var type = Character.getType(character);
            return type == Character.MATH_SYMBOL ||
                    type == Character.CURRENCY_SYMBOL ||
                    type == Character.MODIFIER_SYMBOL ||
                    type == Character.OTHER_SYMBOL;
        };
    }
}
