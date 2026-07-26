package com.quan.diabetes.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchUtilTest {

    @Test
    @DisplayName("Should test class constructor")
    void testConstructor() {
        SearchUtil util = new SearchUtil();
        assertNotNull(util);
    }

    @Nested
    @DisplayName("removeAccents tests")
    class RemoveAccentsTests {

        @Test
        void testRemoveAccents_Null() {
            assertNull(SearchUtil.removeAccents(null));
        }

        @Test
        void testRemoveAccents_WithAccentsAndSpecialCharacters() {
            assertEquals("Tieng Viet", SearchUtil.removeAccents("Tiếng Việt"));
            assertEquals("Dac Lac", SearchUtil.removeAccents("Đắc Lắc"));
            assertEquals("duong huyet Dac Lac", SearchUtil.removeAccents("đường huyết Đắc Lắc"));
        }

        @Test
        void testRemoveAccents_WithoutAccents() {
            assertEquals("Hello World 123", SearchUtil.removeAccents("Hello World 123"));
        }
    }

    @Nested
    @DisplayName("matches tests")
    class MatchesTests {

        @Test
        void testMatches_NullInputs() {
            assertFalse(SearchUtil.matches(null, "keyword"));
            assertFalse(SearchUtil.matches("text", null));
            assertFalse(SearchUtil.matches(null, null));
        }

        @Test
        void testMatches_MatchingCases() {
            assertTrue(SearchUtil.matches("Pham Van Dong", "pham"));
            assertTrue(SearchUtil.matches("Phạm Văn Đồng", "van"));
            assertTrue(SearchUtil.matches("Phạm Văn Đồng", "  ĐỒNG  "));
        }

        @Test
        void testMatches_NonMatchingCases() {
            assertFalse(SearchUtil.matches("Phạm Văn Đồng", "Nguyen"));
            assertFalse(SearchUtil.matches("Phạm Văn Đồng", "abc"));
        }
    }

    @Nested
    @DisplayName("startsWith tests")
    class StartsWithTests {

        @Test
        void testStartsWith_NullInputs() {
            assertFalse(SearchUtil.startsWith(null, "keyword"));
            assertFalse(SearchUtil.startsWith("text", null));
            assertFalse(SearchUtil.startsWith(null, null));
        }

        @Test
        void testStartsWith_MatchingCases() {
            assertTrue(SearchUtil.startsWith("Pham Van Dong", "pham"));
            assertTrue(SearchUtil.startsWith("Phạm Văn Đồng", "  PHẠM  "));
            assertTrue(SearchUtil.startsWith("Đắc Lắc", "dac"));
        }

        @Test
        void testStartsWith_NonMatchingCases() {
            // Contained but does not start with
            assertFalse(SearchUtil.startsWith("Phạm Văn Đồng", "van"));
            assertFalse(SearchUtil.startsWith("Phạm Văn Đồng", "dong"));
            assertFalse(SearchUtil.startsWith("Phạm Văn Đồng", "Nguyen"));
        }
    }
}
