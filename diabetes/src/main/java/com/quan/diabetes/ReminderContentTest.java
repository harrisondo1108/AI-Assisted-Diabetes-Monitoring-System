package com.quan.diabetes;

import com.quan.diabetes.service.reminder.MedicationSchedualeService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ReminderContentTest {

    public static void main(String[] args) {

        ConfigurableApplicationContext context =
                SpringApplication.run(ReminderContentTest.class, args);

        MedicationSchedualeService service =
                context.getBean(MedicationSchedualeService.class);

        service.generateReminder("EXM-1782060867382-993");
        System.out.println("============Suscessful==============");

    }
}
