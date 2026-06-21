package com.quan.diabetes;

import com.quan.diabetes.entity.PatientRoutine;
import com.quan.diabetes.service.reminder.ReminderScheduledTask;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class AIReminderScheduledTaskTest {

    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                SpringApplication.run(AIReminderScheduledTaskTest.class, args);

        ReminderScheduledTask scheduledTask =
                context.getBean(ReminderScheduledTask.class);
        PatientRoutine pr = new PatientRoutine();
        scheduledTask.scanDueReminders();
        System.out.println("============ AI Reminder Scheduled Task Test Successful ==============");

        context.close();
    }
}
