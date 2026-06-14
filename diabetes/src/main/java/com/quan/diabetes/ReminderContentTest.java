package com.quan.diabetes;

import com.quan.diabetes.service.AIService.ReminderContentService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ReminderContentTest {

    public static void main(String[] args) {

        ConfigurableApplicationContext context =
                SpringApplication.run(ReminderContentTest.class, args);

        ReminderContentService service =
                context.getBean(ReminderContentService.class);

        service.generateReminder("CE001");
        System.out.println("============Suscessful==============");

    }
}
