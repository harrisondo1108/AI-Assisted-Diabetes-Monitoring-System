package com.quan.diabetes;

import com.quan.diabetes.entity.PatientRoutine;
import com.quan.diabetes.service.AIService.ReminderContentService;
import com.quan.diabetes.service.AIService.TimingReminderConfig;
import com.quan.diabetes.util.ReminderTimeCalculator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.LocalTime;

@SpringBootApplication
public class TimingConfigTest {
    public static void main(String[] args) {

//        ConfigurableApplicationContext context =
//                SpringApplication.run(TimingConfigTest.class, args);
//
//        TimingReminderConfig service = context.getBean(TimingReminderConfig.class);

        String time = "sau thức dậy";
        PatientRoutine pr = new PatientRoutine();
        pr.setBreakfastTime(LocalTime.of(8, 0));
        System.out.println(time);
        System.out.println(pr);
//        String result = ReminderTimeCalculator.calculateReminderTimeString(time, pr);
//
//        System.out.println("============Timing Reminder==============");
//        System.out.println(result);
    }
}
