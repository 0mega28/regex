package com.example.regex;

@FunctionalInterface
public interface CharacterSet {

    // Common predefined sets as static methods
    CharacterSet decimalDigit = Character::isDigit;
    CharacterSet whiteSpaces = Character::isWhitespace;
    CharacterSet alphaNumeric = Character::isLetterOrDigit;
    CharacterSet word = alphaNumeric.union(charactersIn("_"));
    CharacterSet punctuationCharacters = character -> {
        var type = Character.getType(character);
        return type == Character.CONNECTOR_PUNCTUATION ||
               type == Character.DASH_PUNCTUATION ||
               type == Character.START_PUNCTUATION ||
               type == Character.END_PUNCTUATION ||
               type == Character.OTHER_PUNCTUATION ||
               type == Character.INITIAL_QUOTE_PUNCTUATION ||
               type == Character.FINAL_QUOTE_PUNCTUATION;
    };
    CharacterSet capitalizedLetters = Character::isUpperCase;
    CharacterSet lowerCaseCharacters = Character::isLowerCase;
    CharacterSet nonBaseCharacters = character -> Character.getType(character) == Character.NON_SPACING_MARK;
    CharacterSet symbols = character -> {
        var type = Character.getType(character);
        return type == Character.MATH_SYMBOL ||
               type == Character.CURRENCY_SYMBOL ||
               type == Character.MODIFIER_SYMBOL ||
               type == Character.OTHER_SYMBOL;
    };

    static CharacterSet charactersIn(String allowed) {
        return character -> allowed.indexOf(character) != -1;
    }

    boolean contains(char character);

    default CharacterSet union(CharacterSet other) {
        return character -> CharacterSet.this.contains(character) || other.contains(character);
    }

    default CharacterSet inverted() {
        return character -> !CharacterSet.this.contains(character);
    }
}
