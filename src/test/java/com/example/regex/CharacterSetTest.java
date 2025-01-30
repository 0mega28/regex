package com.example.regex;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CharacterSetTest {

    @Test
    void testDecimalDigit() {
        CharacterSet digits = CharacterSet.decimalDigit;

        assertTrue(digits.contains('0'));
        assertTrue(digits.contains('5'));
        assertTrue(digits.contains('9'));

        assertFalse(digits.contains('a'));
        assertFalse(digits.contains(' '));
        assertFalse(digits.contains('#'));
    }

    @Test
    void testWhiteSpaces() {
        CharacterSet whitespaces = CharacterSet.whiteSpaces;

        assertTrue(whitespaces.contains(' '));
        assertTrue(whitespaces.contains('\t'));
        assertTrue(whitespaces.contains('\n'));
        assertTrue(whitespaces.contains('\s'));

        assertFalse(whitespaces.contains('A'));
        assertFalse(whitespaces.contains('1'));
    }

    @Test
    void testAlphaNumeric() {
        CharacterSet alphanumeric = CharacterSet.alphaNumeric;

        assertTrue(alphanumeric.contains('A'));
        assertTrue(alphanumeric.contains('z'));
        assertTrue(alphanumeric.contains('0'));
        assertTrue(alphanumeric.contains('9'));

        assertFalse(alphanumeric.contains('@'));
        assertFalse(alphanumeric.contains(' '));
    }

    @Test
    void testWord() {
        CharacterSet word = CharacterSet.word;

        assertTrue(word.contains('A'));
        assertTrue(word.contains('z'));
        assertTrue(word.contains('0'));
        assertTrue(word.contains('9'));
        assertTrue(word.contains('_'));  // Underscore is part of word characters

        assertFalse(word.contains('-'));
        assertFalse(word.contains(' '));
    }

    @Test
    void testPunctuationCharacters() {
        CharacterSet punctuation = CharacterSet.punctuationCharacters;

        assertTrue(punctuation.contains(','));
        assertTrue(punctuation.contains('.'));
        assertTrue(punctuation.contains('!'));
        assertTrue(punctuation.contains('?'));
        assertTrue(punctuation.contains(';'));
        assertTrue(punctuation.contains('-'));

        assertFalse(punctuation.contains('A'));
        assertFalse(punctuation.contains(' '));
        assertFalse(punctuation.contains('1'));
    }

    @Test
    void testCapitalizedLetters() {
        CharacterSet caps = CharacterSet.capitalizedLetters;

        assertTrue(caps.contains('A'));
        assertTrue(caps.contains('Z'));

        assertFalse(caps.contains('a'));
        assertFalse(caps.contains(' '));
        assertFalse(caps.contains('1'));
    }

    @Test
    void testLowerCaseCharacters() {
        CharacterSet lower = CharacterSet.lowerCaseCharacters;

        assertTrue(lower.contains('a'));
        assertTrue(lower.contains('z'));

        assertFalse(lower.contains('A'));
        assertFalse(lower.contains(' '));
        assertFalse(lower.contains('1'));
    }

    @Test
    void testNonBaseCharacters() {
        CharacterSet nonBase = CharacterSet.nonBaseCharacters;

        // These are combining diacritical marks (Unicode category: NON_SPACING_MARK)
        assertTrue(nonBase.contains('\u0301')); // ´ (Acute accent)
        assertTrue(nonBase.contains('\u0300')); // ̀ (Grave accent)
        assertTrue(nonBase.contains('\u0327')); // ̧ (Cedilla)

        // Regular letters and symbols should NOT be considered non-base characters
        assertFalse(nonBase.contains('A'));
        assertFalse(nonBase.contains('1'));
        assertFalse(nonBase.contains('.'));
    }

    @Test
    void testSymbols() {
        CharacterSet symbols = CharacterSet.symbols;

        // Currency symbols
        assertTrue(symbols.contains('$'));
        assertTrue(symbols.contains('₹'));
        assertTrue(symbols.contains('€'));

        // Mathematical symbols
        assertTrue(symbols.contains('+'));
        // assertTrue(symbols.contains('-'));  // Only for proper math symbols, hyphen-minus is excluded
        assertTrue(symbols.contains('÷'));
        assertTrue(symbols.contains('∑'));

        // Other symbols
        assertTrue(symbols.contains('©')); // Copyright symbol
        assertTrue(symbols.contains('™')); // Trademark symbol

        // Regular characters should NOT be included in symbols
        assertFalse(symbols.contains('A'));
        assertFalse(symbols.contains('1'));
        assertFalse(symbols.contains('.'));

        // Hyphen should not be considered a symbol (it is a punctuation mark)
        assertFalse(symbols.contains('-'));
    }

    @Test
    void testUnion() {
        CharacterSet letters = CharacterSet.alphaNumeric;
        CharacterSet symbols = CharacterSet.charactersIn("@#");

        CharacterSet combined = letters.union(symbols);

        assertTrue(combined.contains('A'));
        assertTrue(combined.contains('1'));
        assertTrue(combined.contains('@'));
        assertTrue(combined.contains('#'));

        assertFalse(combined.contains(' '));
        assertFalse(combined.contains('&'));
    }

    @Test
    void testInverted() {
        CharacterSet letters = CharacterSet.alphaNumeric;
        CharacterSet nonLetters = letters.inverted();

        assertFalse(nonLetters.contains('A'));
        assertFalse(nonLetters.contains('z'));
        assertFalse(nonLetters.contains('0'));

        assertTrue(nonLetters.contains(' '));
        assertTrue(nonLetters.contains('@'));
    }
}
