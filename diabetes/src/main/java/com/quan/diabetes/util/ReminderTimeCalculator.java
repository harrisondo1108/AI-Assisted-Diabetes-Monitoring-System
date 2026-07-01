package com.quan.diabetes.util;

import com.quan.diabetes.entity.PatientRoutine;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Utility to calculate reminder send time from a provided time slot description and PatientRoutine.
 * Business rule: when the slot indicates "trước" (before) use routineTime - 30 minutes;
 * when it indicates "sau" (after) use routineTime + 30 minutes.
 */

public final class ReminderTimeCalculator {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private ReminderTimeCalculator() {}

    /**
     * Calculate reminder time string in HH:mm format.
     * If timeSlot is already an HH:mm string, it will be returned (validated).
     * If the timeSlot contains keywords like "trước" or "sau" and a meal (sáng/trưa/tối),
     * it will base on the corresponding time in {@code routine} and apply +/-30 minutes.
     * If no match found, returns the provided timeSlot if parseable, otherwise the routine's breakfastTime.
     */
    public static LocalDateTime toLocalDateTime(LocalTime time, LocalDate date) {
        return LocalDateTime.of(date, time);
    }

    public static LocalTime calculateReminderTime(String timeSlot, PatientRoutine routine) {
        if (timeSlot == null) timeSlot = "";
        String s = timeSlot.trim().toLowerCase(Locale.ROOT);

        if(routine == null){
            routine = new PatientRoutine();
        }
        // Try parse explicit HH:mm
        try {
            if (s.matches("^\\d{1,2}:\\d{2}$")) {
                return LocalTime.parse(s, TIME_FORMATTER);
            }
        } catch (DateTimeParseException ignored) {
        }

        boolean isBefore = s.contains("trước") || s.contains("truoc") || s.contains("before");
        boolean isAfter = s.contains("sau") || s.contains("after");

        LocalTime base = null;

        // meal/time keywords (Vietnamese + English)
        if (s.contains("sáng") || s.contains("sang") || s.contains("breakfast") || s.contains("ăn sáng") || s.contains("an sang")) {
            base = routine != null ? routine.getBreakfastTime() : null;
        } else if (s.contains("trưa") || s.contains("lunch") || s.contains("ăn trưa") || s.contains("an trua")) {
            base = routine != null ? routine.getLunchTime() : null;
        } else if (s.contains("tối") || s.contains("toi") || s.contains("dinner") || s.contains("ăn tối") || s.contains("an toi")) {
            base = routine != null ? routine.getDinnerTime() : null;
        } else if (s.contains("ngủ") || s.contains("sleep") || s.contains("bed")) {
            base = routine != null ? routine.getSleepTime() : null;
        } else if (s.contains("thức") || s.contains("wake") || s.contains("wakeup") || s.contains("wakup")) {
            base = routine != null ? routine.getWakeUpTime() : null;
        }

        // If still no base, try to treat timeSlot like a general descriptor: "trước ăn" -> breakfast
        if (base == null) {
            if (s.contains("ăn")) {
                // default to breakfast for generic "ăn"
                base = routine != null ? routine.getBreakfastTime() : null;
            }
        }

        // Fallback: if still null, use breakfastTime or current time
        if (base == null) {
            base = routine != null ? routine.getBreakfastTime() : LocalTime.now();
        }

        if (isBefore) {
            return base.minusMinutes(30);
        } else if (isAfter) {
            return base.plusMinutes(30);
        } else {
            // no before/after specified -> return base as-is
            return base;
        }
    }
}

