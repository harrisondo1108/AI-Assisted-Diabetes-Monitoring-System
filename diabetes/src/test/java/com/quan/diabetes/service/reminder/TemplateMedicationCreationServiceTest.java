package com.quan.diabetes.service.reminder;

import com.quan.diabetes.dto.reminder.PrescriptionReminderDto;
import com.quan.diabetes.entity.TreatmentPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TemplateMedicationCreationServiceTest {

    private TemplateMedicationCreationService service;

    @BeforeEach
    void setUp() {
        service = new TemplateMedicationCreationService();
    }

    private PrescriptionReminderDto createDto(String medicationName, String dosage, LocalDate startDate, LocalDate endDate, String form, String route, String instruction, String timingName, String plan, TreatmentPlan treatmentPlan) {
        return new PrescriptionReminderDto("p1", "exam1", medicationName, dosage, startDate, endDate, form, route, instruction, timingName, plan, treatmentPlan);
    }

    @Test
    @DisplayName("Should format group reminder when patientName, timeSlot and medicines are null")
    void generateGroupReminder_NullInputs() {
        String resultNull = service.generateGroupReminder(null, null, null);

        assertTrue(resultNull.contains("Chào bạn nhé,"));
        assertTrue(resultNull.contains("Đến giờ uống thuốc rồi ạ."));
        assertTrue(resultNull.contains("- Hiện tại chưa có thông tin thuốc cần uống trong khung giờ này."));
    }

    @Test
    @DisplayName("Should format group reminder when patientName, timeSlot are blank and medicines is empty list")
    void generateGroupReminder_BlankAndEmptyInputs() {
        String resultBlank = service.generateGroupReminder("   ", "   ", Collections.emptyList());

        assertTrue(resultBlank.contains("Chào bạn nhé,"));
        assertTrue(resultBlank.contains("Đến giờ uống thuốc rồi ạ."));
        assertTrue(resultBlank.contains("- Hiện tại chưa có thông tin thuốc cần uống trong khung giờ này."));
    }

    @Test
    @DisplayName("Should format group reminder with timeSlot and null medicine element in list")
    void generateGroupReminder_WithTimeSlotAndNullMedicineInList() {
        List<PrescriptionReminderDto> list = new ArrayList<>();
        list.add(null);

        String result = service.generateGroupReminder("Nguyễn Văn A", "  08:00  ", list);

        assertTrue(result.contains("Chào bạn Nguyễn Văn A,"));
        assertTrue(result.contains("Đến giờ uống thuốc (08:00) rồi ạ."));
        assertTrue(result.contains("- (Thông tin thuốc chưa rõ)"));
        assertFalse(result.contains("Một vài lưu ý nhỏ cho ngày hôm nay"));
    }

    @Test
    @DisplayName("Should format medicine line with default medication name and all combination of form/route")
    void generateGroupReminder_MedicineFormAndRouteCombinations() {
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 5);

        // Medicine 1: Default name (null), both form & route present
        PrescriptionReminderDto m1 = createDto(null, "1 viên", start, end, "Viên nén", "Đường uống", "Uống sau khi ăn", "sáng", "Dùng liên tục 7 ngày", null);

        // Medicine 2: Name present, only form present
        PrescriptionReminderDto m2 = createDto("Metformin 500mg", null, start, end, "Viên bao phim", null, null, "sáng", null, null);

        // Medicine 3: Name present, only route present
        PrescriptionReminderDto m3 = createDto("Insulin", null, start, end, "  ", "Tiêm dưới da", null, "sáng", null, null);

        // Medicine 4: Name present, neither form nor route present
        PrescriptionReminderDto m4 = createDto("Aspirin", null, start, end, null, null, null, "sáng", null, null);

        List<PrescriptionReminderDto> list = List.of(m1, m2, m3, m4);

        String result = service.generateGroupReminder("Bệnh nhân B", "buổi sáng", list);

        assertTrue(result.contains("Thuốc chưa rõ tên | 1 viên | Viên nén, Đường uống | Uống sau khi ăn | Dùng liên tục 7 ngày"));
        assertTrue(result.contains("Metformin 500mg | Viên bao phim"));
        assertTrue(result.contains("Insulin | Tiêm dưới da"));
        assertTrue(result.contains("Aspirin"));
    }

    @Test
    @DisplayName("Should include treatment plan notes when present in prescription")
    void generateGroupReminder_WithTreatmentPlan() {
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 5);

        TreatmentPlan plan = new TreatmentPlan();
        plan.setTreatmentGoal("  Giảm HBA1c xuống dưới 7%  ");
        plan.setDietPlan("Ăn ít tinh bột");
        plan.setExercisePlan("Đi bộ 30 phút/ngày");
        plan.setGlucoseMonitoringPlan("Đo đường huyết trước ăn sáng");

        PrescriptionReminderDto dto = createDto("Glimepiride 2mg", "1 viên", start, end, "Viên", "Uống", "Trước ăn", "sáng", "Kế hoạch 1", plan);
        PrescriptionReminderDto dtoNullPlan = createDto("Thuốc 2", null, start, end, null, null, null, "sáng", null, null);

        List<PrescriptionReminderDto> list = List.of(dto, dtoNullPlan);

        String result = service.generateGroupReminder("Chị Mai", "buổi tối", list);

        assertTrue(result.contains("Một vài lưu ý nhỏ cho ngày hôm nay để việc điều trị hiệu quả hơn:"));
        assertTrue(result.contains("🎯 Mục tiêu điều trị: Giảm HBA1c xuống dưới 7%"));
        assertTrue(result.contains("🥗 Chế độ dinh dưỡng: Ăn ít tinh bột"));
        assertTrue(result.contains("🏃 Chế độ tập luyện: Đi bộ 30 phút/ngày"));
        assertTrue(result.contains("🩸 Theo dõi đường huyết: Đo đường huyết trước ăn sáng"));
        assertTrue(result.contains("Chúc bạn luôn khỏe mạnh và có một ngày tốt lành!"));
    }

    @Test
    @DisplayName("Should skip blank fields in TreatmentPlan")
    void generateGroupReminder_WithPartialTreatmentPlan() {
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 5);

        TreatmentPlan plan = new TreatmentPlan();
        plan.setTreatmentGoal("  Giảm HBA1c  ");
        plan.setDietPlan(null); // Blank
        plan.setExercisePlan("   "); // Blank
        plan.setGlucoseMonitoringPlan("Đo đường huyết");

        PrescriptionReminderDto dto = createDto("Thuốc X", "1 viên", start, end, "Viên", "Uống", "Trước ăn", "sáng", "Kế hoạch", plan);

        String result = service.generateGroupReminder("Anh Nam", "buổi sáng", List.of(dto));

        assertTrue(result.contains("🎯 Mục tiêu điều trị: Giảm HBA1c"));
        assertFalse(result.contains("Chế độ dinh dưỡng"));
        assertFalse(result.contains("Chế độ tập luyện"));
        assertTrue(result.contains("🩸 Theo dõi đường huyết: Đo đường huyết"));
    }
}
