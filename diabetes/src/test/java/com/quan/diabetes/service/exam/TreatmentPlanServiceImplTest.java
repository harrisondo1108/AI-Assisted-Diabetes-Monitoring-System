package com.quan.diabetes.service.exam;

import com.quan.diabetes.entity.TreatmentPlan;
import com.quan.diabetes.repository.TreatmentPlanRepository;
import com.quan.diabetes.service.exam.impl.TreatmentPlanServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TreatmentPlanServiceImplTest {

    @Mock
    private TreatmentPlanRepository treatmentPlanRepository;

    @InjectMocks
    private TreatmentPlanServiceImpl treatmentPlanService;

    @Test
    @DisplayName("findByClinicalExamId - Should return treatment plan optional when exists")
    void findByClinicalExamId_Success() {
        TreatmentPlan plan = new TreatmentPlan();
        when(treatmentPlanRepository.findByClinicalExam_ClinicalExamId("EX000001"))
                .thenReturn(Optional.of(plan));

        Optional<TreatmentPlan> result = treatmentPlanService.findByClinicalExamId("EX000001");
        assertTrue(result.isPresent());
        assertEquals(plan, result.get());
    }

    @Test
    @DisplayName("findByClinicalExamId - Should return empty optional when not exists")
    void findByClinicalExamId_NotFound() {
        when(treatmentPlanRepository.findByClinicalExam_ClinicalExamId("EX999999"))
                .thenReturn(Optional.empty());

        Optional<TreatmentPlan> result = treatmentPlanService.findByClinicalExamId("EX999999");
        assertTrue(result.isEmpty());
    }
}
