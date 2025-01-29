package com.example.regex;

interface CharacterSet {
    boolean contains(char character);

    static CharacterSet charactersIn(String allowed) {
        return new CharacterSet() {
            @Override
            public boolean contains(char character) {
                return allowed.indexOf(character) != -1;
            }
        };
    }

    default CharacterSet union(CharacterSet other) {
        return new CharacterSet() {
            @Override
            public boolean contains(char character) {
                return CharacterSet.this.contains(character) || other.contains(character);
            }
        };
    }

    default CharacterSet inverted() {
        return new CharacterSet() {
            @Override
            public boolean contains(char character) {
                return !CharacterSet.this.contains(character);
            }
        };
    }

    class DecimalDigit implements CharacterSet {

        @Override
        public boolean contains(char character) {
            return Character.isDigit(character);
        }
    }

    class WhiteSpaces implements CharacterSet {

        @Override
        public boolean contains(char character) {
            return Character.isWhitespace(character);
        }
    }

    class AlphaNumeric implements CharacterSet {

        @Override
        public boolean contains(char character) {
            return Character.isLetterOrDigit(character);
        }
    }

    class Word implements CharacterSet {

        private static final CharacterSet WORD_SET = new AlphaNumeric().union(CharacterSet.charactersIn("_"));

        @Override
        public boolean contains(char character) {
            return WORD_SET.contains(character);
        }

    }

    class PunctuationCharacters implements CharacterSet {

        @Override
        public boolean contains(char character) {
            var type = Character.getType(character);
            return type == Character.CONNECTOR_PUNCTUATION ||
                    type == Character.DASH_PUNCTUATION ||
                    type == Character.START_PUNCTUATION ||
                    type == Character.END_PUNCTUATION ||
                    type == Character.OTHER_PUNCTUATION ||
                    type == Character.INITIAL_QUOTE_PUNCTUATION ||
                    type == Character.FINAL_QUOTE_PUNCTUATION;

        }
    }

    class CapitalizedLetters implements CharacterSet {

        @Override
        public boolean contains(char character) {
            return Character.isUpperCase(character);
        }
    }

    class LowerCaseCharacters implements CharacterSet {

        @Override
        public boolean contains(char character) {
            return Character.isLowerCase(character);
        }
    }

    class NonBaseCharacters implements CharacterSet {

        @Override
        public boolean contains(char character) {
            return Character.getType(character) == Character.NON_SPACING_MARK;
        }
    }

    class Symbols implements CharacterSet {

        @Override
        public boolean contains(char character) {
            var type = Character.getType(character);
            return type == Character.MATH_SYMBOL ||
                    type == Character.CURRENCY_SYMBOL ||
                    type == Character.MODIFIER_SYMBOL ||
                    type == Character.OTHER_SYMBOL;
        }
    }
}
