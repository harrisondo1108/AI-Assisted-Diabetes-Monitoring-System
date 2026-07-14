package com.quan.diabetes.util;

import com.quan.diabetes.entity.PatientRoutine;
import org.junit.jupiter.api.Test;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReminderTimeCalculatorTest {

    @Test
    void testCalculateReminderTime_KhiCan() {
        PatientRoutine routine = new PatientRoutine();
        // Set some routine times just to be sure
        routine.setBreakfastTime(LocalTime.of(8, 0));

        // Test with "Khi cần"
        LocalTime result1 = ReminderTimeCalculator.calculateReminderTime("Khi cần", routine);
        assertEquals(LocalTime.of(7, 30), result1);

        // Test with "khi can"
        LocalTime result2 = ReminderTimeCalculator.calculateReminderTime("khi can", routine);
        assertEquals(LocalTime.of(7, 30), result2);

        // Test with "Khi cần uống"
        LocalTime result3 = ReminderTimeCalculator.calculateReminderTime("Khi cần uống", routine);
        assertEquals(LocalTime.of(7, 30), result3);

        // Test with "as needed"
        LocalTime result4 = ReminderTimeCalculator.calculateReminderTime("as needed", routine);
        assertEquals(LocalTime.of(7, 30), result4);
    }

    @Test
    void testCalculateReminderTime_OtherCases() {
        PatientRoutine routine = new PatientRoutine();
        routine.setBreakfastTime(LocalTime.of(8, 0));

        // Test normal time slot format HH:mm
        LocalTime result1 = ReminderTimeCalculator.calculateReminderTime("09:15", routine);
        assertEquals(LocalTime.of(9, 15), result1);

        // Test before breakfast
        LocalTime result2 = ReminderTimeCalculator.calculateReminderTime("Trước ăn sáng", routine);
        assertEquals(LocalTime.of(7, 30), result2);
    }
}
