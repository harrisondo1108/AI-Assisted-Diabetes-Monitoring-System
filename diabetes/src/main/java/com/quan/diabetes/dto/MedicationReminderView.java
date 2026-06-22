package com.quan.diabetes.dto;

import java.time.LocalDateTime;

public class MedicationReminderView {
    private final String title;
    private final String message;
    private final String medicationName;
    private final String dosage;
    private final String timingName;
    private final String instruction;
    private final String medicationPlan;
    private final LocalDateTime medicationTime;
    private final LocalDateTime reminderTime;
    private final int minutesBefore;
    private final boolean dueNow;
    private final boolean past;

    public MedicationReminderView(String title,
                                  String message,
                                  String medicationName,
                                  String dosage,
                                  String timingName,
                                  String instruction,
                                  String medicationPlan,
                                  LocalDateTime medicationTime,
                                  LocalDateTime reminderTime,
                                  int minutesBefore,
                                  boolean dueNow,
                                  boolean past) {
        this.title = title;
        this.message = message;
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.timingName = timingName;
        this.instruction = instruction;
        this.medicationPlan = medicationPlan;
        this.medicationTime = medicationTime;
        this.reminderTime = reminderTime;
        this.minutesBefore = minutesBefore;
        this.dueNow = dueNow;
        this.past = past;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public String getDosage() {
        return dosage;
    }

    public String getTimingName() {
        return timingName;
    }

    public String getInstruction() {
        return instruction;
    }

    public String getMedicationPlan() {
        return medicationPlan;
    }

    public LocalDateTime getMedicationTime() {
        return medicationTime;
    }

    public LocalDateTime getReminderTime() {
        return reminderTime;
    }

    public int getMinutesBefore() {
        return minutesBefore;
    }

    public boolean isDueNow() {
        return dueNow;
    }

    public boolean isPast() {
        return past;
    }
}
