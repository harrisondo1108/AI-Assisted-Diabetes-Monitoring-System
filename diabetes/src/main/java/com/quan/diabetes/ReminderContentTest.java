package com.quan.diabetes;

import com.quan.diabetes.service.reminder.ReminderSchedualeService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ReminderContentTest {

    public static void main(String[] args) {

        ConfigurableApplicationContext context =
                SpringApplication.run(ReminderContentTest.class, args);

        ReminderSchedualeService service =
                context.getBean(ReminderSchedualeService.class);

        service.generateReminder("CE001");
        System.out.println("============Suscessful==============");

    }
}
