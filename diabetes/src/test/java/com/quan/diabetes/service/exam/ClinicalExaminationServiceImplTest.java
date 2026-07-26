package com.quan.diabetes.service.exam;

import com.quan.diabetes.dto.doctor.ExamStep1Form;
import com.quan.diabetes.dto.doctor.ExamStep2Form;
import com.quan.diabetes.dto.doctor.ExamStep3Form;
import com.quan.diabetes.dto.doctor.PrescriptionLineDTO;
import com.quan.diabetes.entity.*;
import com.quan.diabetes.repository.*;
import com.quan.diabetes.service.exam.impl.ClinicalExaminationServiceImpl;
import com.quan.diabetes.service.reminder.AppointmentSchedule;
import com.quan.diabetes.service.reminder.MedicationSchedualeService;
import com.quan.diabetes.service.systemlog.SystemLogService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClinicalExaminationServiceImplTest {

    @Mock
    private ClinicalExaminationRepository clinicalExaminationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private SymptomsCatalogRepository symptomsCatalogRepository;
    @Mock
    private ExamSymptomRepository examSymptomRepository;
    @Mock
    private PrescriptionRepository prescriptionRepository;
    @Mock
    private PrescriptionDetailRepository prescriptionDetailRepository;
    @Mock
    private MedicationRepository medicationRepository;
    @Mock
    private MedicationTimingRepository medicationTimingRepository;
    @Mock
    private PrescriptionTimingRepository prescriptionTimingRepository;
    @Mock
    private TreatmentPlanRepository treatmentPlanRepository;
    @Mock
    private LabOrderRepository labOrderRepository;
    @Mock
    private LabResultRepository labResultRepository;
    @Mock
    private LabTestCatalogRepository labTestCatalogRepository;
    @Mock
    private PatientTypeRepository patientTypeRepository;
    @Mock
    private IndicatorThresholdRepository indicatorThresholdRepository;
    @Mock
    private MedicationSchedualeService medicationSchedualeService;
    @Mock
    private AppointmentSchedule appointmentSchedule;
    @Mock
    private ReminderRepository reminderRepository;
    @Mock
    private SystemLogService systemLogService;

    @InjectMocks
    private ClinicalExaminationServiceImpl clinicalExaminationService;

    @Test
    @DisplayName("findAll - Should return all clinical examinations")
    void findAll_Success() {
        ClinicalExamination exam = new ClinicalExamination();
        when(clinicalExaminationRepository.findAll()).thenReturn(List.of(exam));

        List<ClinicalExamination> result = clinicalExaminationService.findAll();
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("findById - Should return optional examination")
    void findById_Success() {
        ClinicalExamination exam = new ClinicalExamination();
        when(clinicalExaminationRepository.findById("EX01")).thenReturn(Optional.of(exam));

        Optional<ClinicalExamination> result = clinicalExaminationService.findById("EX01");
        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("create - Should save entity")
    void create_Success() {
        ClinicalExamination exam = new ClinicalExamination();
        when(clinicalExaminationRepository.save(exam)).thenReturn(exam);

        ClinicalExamination result = clinicalExaminationService.create(exam);
        assertEquals(exam, result);
    }

    @Test
    @DisplayName("update - When exists saves, when missing throws EntityNotFoundException")
    void update_Test() {
        ClinicalExamination exam = new ClinicalExamination();
        when(clinicalExaminationRepository.existsById("EX01")).thenReturn(true);
        when(clinicalExaminationRepository.save(exam)).thenReturn(exam);

        ClinicalExamination updated = clinicalExaminationService.update("EX01", exam);
        assertEquals(exam, updated);

        when(clinicalExaminationRepository.existsById("EX02")).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> clinicalExaminationService.update("EX02", exam));
    }

    @Test
    @DisplayName("deleteById - When exists deletes, when missing throws EntityNotFoundException")
    void deleteById_Test() {
        when(clinicalExaminationRepository.existsById("EX01")).thenReturn(true);
        clinicalExaminationService.deleteById("EX01");
        verify(clinicalExaminationRepository, times(1)).deleteById("EX01");

        when(clinicalExaminationRepository.existsById("EX02")).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> clinicalExaminationService.deleteById("EX02"));
    }

    @Test
    @DisplayName("existsById - Should return repository result")
    void existsById_Success() {
        when(clinicalExaminationRepository.existsById("EX01")).thenReturn(true);
        assertTrue(clinicalExaminationService.existsById("EX01"));
    }

    @Test
    @DisplayName("findByDoctorId & findByPatientId - Should return list of examinations")
    void findByDoctorAndPatient_Success() {
        when(clinicalExaminationRepository.findByDoctor_UserIdOrderByExamDateAsc("DOC01"))
                .thenReturn(List.of(new ClinicalExamination()));
        when(clinicalExaminationRepository.findByPatient_UserIdOrderByExamDateDesc("PAT01"))
                .thenReturn(List.of(new ClinicalExamination()));

        assertEquals(1, clinicalExaminationService.findByDoctorId("DOC01").size());
        assertEquals(1, clinicalExaminationService.findByPatientId("PAT01").size());
    }

    @Test
    @DisplayName("startExamination - Existing exam should update status to InProgress")
    void startExamination_ExistingExam() {
        ClinicalExamination exam = new ClinicalExamination();
        exam.setClinicalExamId("EX000001");
        exam.setStatus("Pending");

        when(clinicalExaminationRepository.findFirstByPatient_UserIdAndDoctor_UserIdAndStatusIn(
                eq("PAT01"), eq("DOC01"), anyList())).thenReturn(Optional.of(exam));
        when(clinicalExaminationRepository.findById("EX000001")).thenReturn(Optional.of(exam));

        clinicalExaminationService.startExamination("PAT01", "DOC01");

        assertEquals("InProgress", exam.getStatus());
        verify(clinicalExaminationRepository).save(exam);
        verify(systemLogService).saveLogWithObject(eq(null), eq("APPROVE_MEDICAL_RECORD"), eq("MedicalRecord"),
                eq("EX000001"), anyString(), any(), any(), eq("SUCCESS"));
    }

    @Test
    @DisplayName("startExamination - New exam when missing should create new and save")
    void startExamination_NewExam() {
        when(clinicalExaminationRepository.findFirstByPatient_UserIdAndDoctor_UserIdAndStatusIn(
                eq("PAT01"), eq("DOC01"), anyList())).thenReturn(Optional.empty());
        when(clinicalExaminationRepository.existsById(anyString())).thenReturn(false);

        Patient patient = new Patient();
        patient.setUserId("PAT01");
        User doctor = new User();
        doctor.setUserId("DOC01");

        when(patientRepository.findById("PAT01")).thenReturn(Optional.of(patient));
        when(userRepository.findById("DOC01")).thenReturn(Optional.of(doctor));

        clinicalExaminationService.startExamination("PAT01", "DOC01");

        verify(clinicalExaminationRepository).save(any(ClinicalExamination.class));
    }

    @Test
    @DisplayName("cancelExamination - Should set status Cancelled and save reason")
    void cancelExamination_Success() {
        ClinicalExamination exam = new ClinicalExamination();
        exam.setClinicalExamId("EX000001");
        exam.setStatus("Pending");

        when(clinicalExaminationRepository.findFirstByPatient_UserIdAndDoctor_UserIdAndStatusIn(
                eq("PAT01"), eq("DOC01"), anyList())).thenReturn(Optional.of(exam));
        when(clinicalExaminationRepository.findById("EX000001")).thenReturn(Optional.of(exam));

        clinicalExaminationService.cancelExamination("PAT01", "Patient not present", "DOC01");

        assertEquals("Cancelled", exam.getStatus());
        assertEquals("Patient not present", exam.getCancelReason());
        assertNull(exam.getDiagnosisNote());
        verify(systemLogService).saveLogWithObject(eq(null), eq("REJECT_MEDICAL_RECORD"), eq("MedicalRecord"),
                eq("EX000001"), anyString(), any(), any(), eq("SUCCESS"));
    }

    @Test
    @DisplayName("requestExamination - When doctor missing should throw exception")
    void requestExamination_DoctorMissing() {
        when(userRepository.findFirstByRole_RoleId("DOC")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                clinicalExaminationService.requestExamination("PAT01", "Fever"));
        assertEquals("Không tìm thấy bác sĩ nào trong hệ thống.", ex.getMessage());
    }

    @Test
    @DisplayName("requestExamination - When active exam exists should throw exception")
    void requestExamination_ActiveExamExists() {
        User doc = new User();
        doc.setUserId("DOC01");
        when(userRepository.findFirstByRole_RoleId("DOC")).thenReturn(Optional.of(doc));
        when(clinicalExaminationRepository.findFirstByPatient_UserIdAndDoctor_UserIdAndStatusIn(
                eq("PAT01"), eq("DOC01"), anyList())).thenReturn(Optional.of(new ClinicalExamination()));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                clinicalExaminationService.requestExamination("PAT01", "Fever"));
        assertEquals("Bạn đã có một yêu cầu khám đang chờ duyệt hoặc một ca khám đang diễn ra.", ex.getMessage());
    }

    @Test
    @DisplayName("requestExamination - Incomplete patient profile should throw PROFILE_INCOMPLETE")
    void requestExamination_IncompleteProfile() {
        User doc = new User();
        doc.setUserId("DOC01");
        when(userRepository.findFirstByRole_RoleId("DOC")).thenReturn(Optional.of(doc));
        when(clinicalExaminationRepository.findFirstByPatient_UserIdAndDoctor_UserIdAndStatusIn(
                eq("PAT01"), eq("DOC01"), anyList())).thenReturn(Optional.empty());

        Patient incomplete = new Patient();
        incomplete.setUserId("PAT01");
        // missing fullName, dob, etc.
        when(patientRepository.findById("PAT01")).thenReturn(Optional.of(incomplete));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                clinicalExaminationService.requestExamination("PAT01", "Fever"));
        assertEquals("PROFILE_INCOMPLETE", ex.getMessage());
    }

    @Test
    @DisplayName("requestExamination - Complete patient profile should create request")
    void requestExamination_Success() {
        User doc = new User();
        doc.setUserId("DOC01");
        when(userRepository.findFirstByRole_RoleId("DOC")).thenReturn(Optional.of(doc));
        when(clinicalExaminationRepository.findFirstByPatient_UserIdAndDoctor_UserIdAndStatusIn(
                eq("PAT01"), eq("DOC01"), anyList())).thenReturn(Optional.empty());

        Patient complete = new Patient();
        complete.setUserId("PAT01");
        complete.setFullName("Nguyen Van A");
        complete.setDob(LocalDate.of(1990, 1, 1));
        complete.setGender(false);
        complete.setHeight(170);
        complete.setWeight(new BigDecimal("65"));
        complete.setPhoneNumber("0987654321");

        when(patientRepository.findById("PAT01")).thenReturn(Optional.of(complete));
        when(clinicalExaminationRepository.existsById(anyString())).thenReturn(false);

        clinicalExaminationService.requestExamination("PAT01", "Pain");
        verify(clinicalExaminationRepository).save(any(ClinicalExamination.class));
        verify(systemLogService).saveLogWithObject(eq("PAT01"), eq("CREATE_MEDICAL_REQUEST"), eq("MedicalRequest"),
                anyString(), anyString(), any(), any(), eq("SUCCESS"));
    }

    @Test
    @DisplayName("getPrescriptionLines - Should parse prescription details into DTOs")
    void getPrescriptionLines_Success() {
        ClinicalExamination exam = new ClinicalExamination();
        exam.setClinicalExamId("EX01");

        Medication med = new Medication();
        med.setMedicationId("MED01");
        med.setMedicationName("Metformin");
        med.setConcentration("500mg");
        med.setForm("Viên nén");

        PrescriptionDetail detail = new PrescriptionDetail();
        detail.setMedication(med);
        detail.setDosage("2 viên/ngày");
        detail.setDurationDays(7);
        detail.setTotalQuantity(14);
        detail.setMedicationPlan("Uống sau ăn");
        detail.setStartDate(LocalDate.of(2026, 1, 1));
        detail.setEndDate(LocalDate.of(2026, 1, 7));

        MedicationTiming timing = new MedicationTiming();
        timing.setTimingName("Sáng");
        PrescriptionTiming pt = new PrescriptionTiming();
        pt.setTiming(timing);
        detail.setPrescriptionTimings(List.of(pt));

        when(prescriptionDetailRepository.findByClinicalExamIdWithDetails("EX01")).thenReturn(List.of(detail));

        List<PrescriptionLineDTO> lines = clinicalExaminationService.getPrescriptionLines("EX01");
        assertEquals(1, lines.size());
        assertEquals("MED01", lines.get(0).getMedId());
        assertEquals("Metformin", lines.get(0).getName());
        assertEquals(2.0, lines.get(0).getDosagePerDose());
        assertEquals("Sáng", lines.get(0).getTimingText());
    }

    @Test
    @DisplayName("saveStep1 - Save symptoms and medical history")
    void saveStep1_Success() {
        ClinicalExamination exam = new ClinicalExamination();
        exam.setClinicalExamId("EX01");
        exam.setStatus("Pending");

        when(clinicalExaminationRepository.findById("EX01")).thenReturn(Optional.of(exam));
        when(clinicalExaminationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        SymptomsCatalog sym = new SymptomsCatalog();
        sym.setSymptomId("SYM01");
        sym.setSymptomName("Khát nước");
        when(symptomsCatalogRepository.findById("SYM01")).thenReturn(Optional.of(sym));

        ExamStep1Form form = new ExamStep1Form();
        form.setMedicalHistory("Tiền sử tiểu đường");
        form.setSymptomIds(List.of("SYM01"));
        form.setSymptomComments(Map.of("SYM01", "Nhiều lần"));

        clinicalExaminationService.saveStep1("EX01", form, "DOC01");

        assertEquals("InProgress", exam.getStatus());
        assertEquals("Tiền sử tiểu đường", exam.getMedicalHistory());
        verify(examSymptomRepository).save(any(ExamSymptom.class));
    }

    @Test
    @DisplayName("saveStep2 - Create lab order and calculate thresholds/flags")
    void saveStep2_Success() {
        ClinicalExamination exam = new ClinicalExamination();
        exam.setClinicalExamId("EX01");
        exam.setPatient(new Patient());

        when(clinicalExaminationRepository.findById("EX01")).thenReturn(Optional.of(exam));

        LabTestCatalog test = new LabTestCatalog();
        test.setLabTestId("TEST01");

        PatientType pt = new PatientType();
        pt.setPatientTypeId(1);

        IndicatorThreshold threshold = new IndicatorThreshold();
        threshold.setMinValue(new BigDecimal("4.0"));
        threshold.setMaxValue(new BigDecimal("6.0"));

        when(indicatorThresholdRepository.findByLabTest_LabTestIdAndPatientType_PatientTypeId("TEST01", 1))
                .thenReturn(Optional.of(threshold));
        when(labOrderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ExamStep2Form form = new ExamStep2Form();
        form.setDiagnosisNote("Chẩn đoán thử");
        form.setOrderLabs(true);

        clinicalExaminationService.saveStep2("EX01", form, pt, List.of(test), "DOC01");

        verify(labOrderRepository).save(any(LabOrder.class));
        verify(labResultRepository).save(any(LabResult.class));
    }

    @Test
    @DisplayName("saveStep3 - Save diagnosis note, appointment date, and treatment plan")
    void saveStep3_Success() {
        ClinicalExamination exam = new ClinicalExamination();
        exam.setClinicalExamId("EX01");
        exam.setStatus("InProgress");

        when(clinicalExaminationRepository.findById("EX01")).thenReturn(Optional.of(exam));
        when(clinicalExaminationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(treatmentPlanRepository.findByClinicalExam_ClinicalExamId("EX01")).thenReturn(Optional.empty());

        ExamStep3Form form = new ExamStep3Form();
        form.setNextAppointment("2026-08-01");
        form.setTreatmentGoal("Giảm HbA1c");
        form.setDietPlan("Ăn ít đường");
        form.setExercisePlan("Đi bộ 30p");
        form.setGlucoseMonitoringPlan("Đo 2 lần/ngày");

        clinicalExaminationService.saveStep3("EX01", form, "DOC01");

        assertEquals(LocalDate.of(2026, 8, 1).atStartOfDay(), exam.getNextAppointment());
        verify(treatmentPlanRepository).save(any(TreatmentPlan.class));
    }

    @Test
    @DisplayName("completeExamination - Save prescription, lock old reminders and generate new ones")
    void completeExamination_Success() {
        ClinicalExamination exam = new ClinicalExamination();
        exam.setClinicalExamId("EX01");

        when(clinicalExaminationRepository.findById("EX01")).thenReturn(Optional.of(exam));
        when(prescriptionRepository.findByClinicalExamination_ClinicalExamId("EX01")).thenReturn(Optional.empty());

        Prescription presc = new Prescription();
        presc.setPrescriptionId("PRC01");
        when(prescriptionRepository.saveAndFlush(any())).thenReturn(presc);

        Medication med = new Medication();
        med.setMedicationId("MED01");
        med.setForm("Viên nén");
        when(medicationRepository.findById("MED01")).thenReturn(Optional.of(med));

        PrescriptionDetail detail = new PrescriptionDetail();
        detail.setPrescriptionDetailId("PRD01");
        when(prescriptionDetailRepository.saveAndFlush(any())).thenReturn(detail);

        Reminder oldReminder = new Reminder();
        oldReminder.setLockStatus(false);
        when(reminderRepository.findByClinicalExamination_ClinicalExamId("EX01")).thenReturn(List.of(oldReminder));

        PrescriptionLineDTO line = new PrescriptionLineDTO();
        line.setMedId("MED01");
        line.setDuration(7);
        line.setQuantity(14);
        line.setDosagePerDose(1.0);
        line.setTiming(List.of("Sáng", "Tối"));
        line.setStartDate("2026-08-01");
        line.setEndDate("2026-08-07");

        clinicalExaminationService.completeExamination("EX01", List.of(line), "DOC01");

        assertEquals("Completed", exam.getStatus());
        assertTrue(oldReminder.getLockStatus());
        verify(medicationSchedualeService).generateReminder("EX01");
        verify(appointmentSchedule).generateAppointmentReminder("EX01");
    }
}
