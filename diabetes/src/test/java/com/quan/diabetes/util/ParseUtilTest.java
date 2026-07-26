package com.quan.diabetes.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ParseUtilTest {

    @Test
    @DisplayName("Should test class constructor")
    void testConstructor() {
        ParseUtil util = new ParseUtil();
        assertNotNull(util);
    }

    @Nested
    @DisplayName("isBlank tests")
    class IsBlankTests {

        @Test
        void testIsBlank_Null() {
            assertTrue(ParseUtil.isBlank(null));
        }

        @Test
        void testIsBlank_Empty() {
            assertTrue(ParseUtil.isBlank(""));
        }

        @Test
        void testIsBlank_WhitespaceOnly() {
            assertTrue(ParseUtil.isBlank("   \t\n  "));
        }

        @Test
        void testIsBlank_NonEmpty() {
            assertFalse(ParseUtil.isBlank("hello"));
            assertFalse(ParseUtil.isBlank("  world  "));
        }
    }

    @Nested
    @DisplayName("parseDate tests")
    class ParseDateTests {

        @Test
        void testParseDate_NullOrBlank() {
            assertNull(ParseUtil.parseDate(null));
            assertNull(ParseUtil.parseDate(""));
            assertNull(ParseUtil.parseDate("   "));
        }

        @Test
        void testParseDate_ValidDate() {
            LocalDate expected = LocalDate.of(2026, 7, 22);
            assertEquals(expected, ParseUtil.parseDate("2026-07-22"));
        }
    }

    @Nested
    @DisplayName("parseGender tests")
    class ParseGenderTests {

        @Test
        void testParseGender_NullOrBlank() {
            assertNull(ParseUtil.parseGender(null));
            assertNull(ParseUtil.parseGender(""));
            assertNull(ParseUtil.parseGender("   "));
        }

        @Test
        void testParseGender_TrueCases() {
            assertTrue(ParseUtil.parseGender("1"));
        }

        @Test
        void testParseGender_FalseCases() {
            assertFalse(ParseUtil.parseGender("0"));
            assertFalse(ParseUtil.parseGender("2"));
            assertFalse(ParseUtil.parseGender("male"));
            assertFalse(ParseUtil.parseGender("female"));
        }
    }

    @Nested
    @DisplayName("parseString tests")
    class ParseStringTests {

        @Test
        void testParseString_NullOrBlank() {
            assertNull(ParseUtil.parseString(null));
            assertNull(ParseUtil.parseString(""));
            assertNull(ParseUtil.parseString("   "));
        }

        @Test
        void testParseString_ValidString() {
            assertEquals("hello", ParseUtil.parseString("hello"));
            assertEquals("trimmed text", ParseUtil.parseString("  trimmed text  "));
        }
    }

    @Nested
    @DisplayName("parseInteger tests")
    class ParseIntegerTests {

        @Test
        void testParseInteger_NullOrBlank() {
            assertNull(ParseUtil.parseInteger(null));
            assertNull(ParseUtil.parseInteger(""));
            assertNull(ParseUtil.parseInteger("   "));
        }

        @Test
        void testParseInteger_ValidNumbers() {
            assertEquals(123, ParseUtil.parseInteger("123"));
            assertEquals(-456, ParseUtil.parseInteger("-456"));
            assertEquals(0, ParseUtil.parseInteger("0"));
        }
    }

    @Nested
    @DisplayName("parseBigDecimal tests")
    class ParseBigDecimalTests {

        @Test
        void testParseBigDecimal_NullOrBlank() {
            assertNull(ParseUtil.parseBigDecimal(null));
            assertNull(ParseUtil.parseBigDecimal(""));
            assertNull(ParseUtil.parseBigDecimal("   "));
        }

        @Test
        void testParseBigDecimal_ValidNumbers() {
            assertEquals(new BigDecimal("123.45"), ParseUtil.parseBigDecimal("123.45"));
            assertEquals(new BigDecimal("-0.001"), ParseUtil.parseBigDecimal("-0.001"));
            assertEquals(new BigDecimal("0"), ParseUtil.parseBigDecimal("0"));
        }
    }

    @Nested
    @DisplayName("isValidPassword tests")
    class IsValidPasswordTests {

        @Test
        void testIsValidPassword_Null() {
            assertFalse(ParseUtil.isValidPassword(null));
        }

        @Test
        void testIsValidPassword_TooShort() {
            // length 7 < 8
            assertFalse(ParseUtil.isValidPassword("Aa1!456"));
        }

        @Test
        void testIsValidPassword_MissingLowercase() {
            assertFalse(ParseUtil.isValidPassword("ABCDEF1!"));
        }

        @Test
        void testIsValidPassword_MissingUppercase() {
            assertFalse(ParseUtil.isValidPassword("abcdef1!"));
        }

        @Test
        void testIsValidPassword_MissingDigit() {
            assertFalse(ParseUtil.isValidPassword("Abcdefgh!"));
        }

        @Test
        void testIsValidPassword_MissingSpecialChar() {
            assertFalse(ParseUtil.isValidPassword("Abcdefg123"));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "ValidP@ssword1",
                "Password123!",
                "Pass123#word",
                "Password$123"
        })
        void testIsValidPassword_ValidPasswords(String password) {
            assertTrue(ParseUtil.isValidPassword(password));
        }
    }
}
