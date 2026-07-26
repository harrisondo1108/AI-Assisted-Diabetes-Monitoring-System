package com.quan.diabetes.util;

import com.quan.diabetes.entity.PatientRoutine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Constructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ReminderTimeCalculatorTest {

    private PatientRoutine routine;

    @BeforeEach
    void setUp() {
        routine = new PatientRoutine();
        routine.setBreakfastTime(LocalTime.of(7, 0));
        routine.setLunchTime(LocalTime.of(12, 0));
        routine.setDinnerTime(LocalTime.of(18, 0));
        routine.setWakeUpTime(LocalTime.of(6, 0));
        routine.setSleepTime(LocalTime.of(22, 0));
    }

    @Test
    @DisplayName("Should test private constructor using reflection")
    void testPrivateConstructor() throws Exception {
        Constructor<ReminderTimeCalculator> constructor = ReminderTimeCalculator.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        ReminderTimeCalculator instance = constructor.newInstance();
        assertNotNull(instance);
    }

    @Test
    @DisplayName("Should test toLocalDateTime conversion")
    void testToLocalDateTime() {
        LocalTime time = LocalTime.of(8, 30);
        LocalDate date = LocalDate.of(2026, 7, 22);
        LocalDateTime expected = LocalDateTime.of(2026, 7, 22, 8, 30);
        assertEquals(expected, ReminderTimeCalculator.toLocalDateTime(time, date));
    }

    @Nested
    @DisplayName("calculateReminderTime tests")
    class CalculateReminderTimeTests {

        @Test
        void testNullTimeSlotAndNullRoutine() {
            // Null timeSlot should default to empty string
            // Null routine should create default PatientRoutine (breakfast 07:00)
            LocalTime result = ReminderTimeCalculator.calculateReminderTime(null, null);
            assertEquals(LocalTime.of(7, 0), result);
        }

        @ParameterizedTest
        @ValueSource(strings = { "Khi cần", "khi can", "as needed", "Khi cần uống" })
        void testAsNeededKeywords(String timeSlot) {
            LocalTime result = ReminderTimeCalculator.calculateReminderTime(timeSlot, routine);
            assertEquals(LocalTime.of(7, 30), result);
        }

        @Test
        void testExplicitTimeFormat() {
            assertEquals(LocalTime.of(9, 15), ReminderTimeCalculator.calculateReminderTime("09:15", routine));
            assertEquals(LocalTime.of(8, 30), ReminderTimeCalculator.calculateReminderTime("08:30", routine));
        }

        @Test
        void testExplicitTimeFormat_InvalidTime_DateTimeParseException() {
            // Matches HH:mm regex pattern ^\d{1,2}:\d{2}$ but fails
            // LocalTime.parse("99:99")
            // Fallback should return breakfast time (07:00)
            assertEquals(LocalTime.of(7, 0), ReminderTimeCalculator.calculateReminderTime("99:99", routine));
        }

        @ParameterizedTest
        @ValueSource(strings = { "sáng", "sang", "breakfast", "ăn sáng", "an sang" })
        void testBreakfastKeywords(String keyword) {
            assertEquals(LocalTime.of(6, 30),
                    ReminderTimeCalculator.calculateReminderTime("trước " + keyword, routine));
            assertEquals(LocalTime.of(7, 30), ReminderTimeCalculator.calculateReminderTime("sau " + keyword, routine));
            assertEquals(LocalTime.of(7, 0), ReminderTimeCalculator.calculateReminderTime(keyword, routine));
        }

        @ParameterizedTest
        @ValueSource(strings = { "trưa", "lunch", "ăn trưa", "an trua" })
        void testLunchKeywords(String keyword) {
            assertEquals(LocalTime.of(11, 30),
                    ReminderTimeCalculator.calculateReminderTime("trước " + keyword, routine));
            assertEquals(LocalTime.of(12, 30), ReminderTimeCalculator.calculateReminderTime("sau " + keyword, routine));
            assertEquals(LocalTime.of(12, 0), ReminderTimeCalculator.calculateReminderTime(keyword, routine));
        }

        @ParameterizedTest
        @ValueSource(strings = { "tối", "toi", "dinner", "ăn tối", "an toi" })
        void testDinnerKeywords(String keyword) {
            assertEquals(LocalTime.of(17, 30),
                    ReminderTimeCalculator.calculateReminderTime("trước " + keyword, routine));
            assertEquals(LocalTime.of(18, 30), ReminderTimeCalculator.calculateReminderTime("sau " + keyword, routine));
            assertEquals(LocalTime.of(18, 0), ReminderTimeCalculator.calculateReminderTime(keyword, routine));
        }

        @ParameterizedTest
        @ValueSource(strings = { "ngủ", "sleep", "bed" })
        void testSleepKeywords(String keyword) {
            assertEquals(LocalTime.of(21, 30),
                    ReminderTimeCalculator.calculateReminderTime("before " + keyword, routine));
            assertEquals(LocalTime.of(22, 30),
                    ReminderTimeCalculator.calculateReminderTime("after " + keyword, routine));
            assertEquals(LocalTime.of(22, 0), ReminderTimeCalculator.calculateReminderTime(keyword, routine));
        }

        @ParameterizedTest
        @ValueSource(strings = { "thức", "wake", "wakeup", "wakup" })
        void testWakeKeywords(String keyword) {
            assertEquals(LocalTime.of(5, 30),
                    ReminderTimeCalculator.calculateReminderTime("truoc " + keyword, routine));
            assertEquals(LocalTime.of(6, 30), ReminderTimeCalculator.calculateReminderTime("sau " + keyword, routine));
            assertEquals(LocalTime.of(6, 0), ReminderTimeCalculator.calculateReminderTime(keyword, routine));
        }

        @Test
        void testGenericMealDescriptor() {
            // "trước ăn" without specific meal -> defaults base to breakfast time (07:00) -
            // 30m = 06:30
            assertEquals(LocalTime.of(6, 30), ReminderTimeCalculator.calculateReminderTime("trước ăn", routine));
            assertEquals(LocalTime.of(7, 30), ReminderTimeCalculator.calculateReminderTime("sau ăn", routine));
            assertEquals(LocalTime.of(7, 0), ReminderTimeCalculator.calculateReminderTime("ăn", routine));
        }

        @Test
        void testUnknownDescriptorFallback() {
            // Unrecognized string -> base falls back to breakfast time (07:00)
            assertEquals(LocalTime.of(7, 0), ReminderTimeCalculator.calculateReminderTime("không xác định", routine));
        }

        @Test
        void testNullBreakfastTimeReturnsNull() {
            routine.setBreakfastTime(null);
            LocalTime result = ReminderTimeCalculator.calculateReminderTime("không xác định", routine);
            assertNull(result);
        }

        @Test
        void testBeforeAndAfterModifiers() {
            // Test "before", "truoc", "trước"
            assertEquals(LocalTime.of(6, 30), ReminderTimeCalculator.calculateReminderTime("before sáng", routine));
            assertEquals(LocalTime.of(6, 30), ReminderTimeCalculator.calculateReminderTime("truoc sáng", routine));
            assertEquals(LocalTime.of(6, 30), ReminderTimeCalculator.calculateReminderTime("trước sáng", routine));

            // Test "after", "sau"
            assertEquals(LocalTime.of(7, 30), ReminderTimeCalculator.calculateReminderTime("after sáng", routine));
            assertEquals(LocalTime.of(7, 30), ReminderTimeCalculator.calculateReminderTime("sau sáng", routine));
        }
    }
}
