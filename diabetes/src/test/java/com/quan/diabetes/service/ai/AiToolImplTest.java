package com.quan.diabetes.service.ai;

import com.quan.diabetes.dto.reminder.PrescriptionReminderDto;
import com.quan.diabetes.entity.*;
import com.quan.diabetes.repository.*;
import com.quan.diabetes.service.ai.impl.AiToolImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiToolImplTest {

    @Mock private PatientRepository patientRepository;
    @Mock private ClinicalExaminationRepository clinicalExaminationRepository;
    @Mock private TreatmentPlanRepository treatmentPlanRepository;
    @Mock private LabResultRepository labResultRepository;
    @Mock private PrescriptionDetailRepository prescriptionDetailRepository;
    @Mock private ExamSymptomRepository examSymptomRepository;

    @InjectMocks
    private AiToolImpl aiTool;

    private Patient patient;
    private ClinicalExamination clinicalExam;
    private TreatmentPlan treatmentPlan;
    private LabResult labResult;
    private PrescriptionDetail prescriptionDetail;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setUserId("PAT-01");
        patient.setFullName("Nguyen Van A");
        patient.setGender(true);
        patient.setHeight(170);
        patient.setWeight(java.math.BigDecimal.valueOf(65.0));
        patient.setBloodgroup("O+");
        patient.setPermanentMedicalHistory("Tieu duong");
        patient.setAllergyNotes("Khong");

        treatmentPlan = new TreatmentPlan();
        treatmentPlan.setTreatmentGoal("Giam duong huyet");
        treatmentPlan.setDietPlan("It tinh bot");
        treatmentPlan.setExercisePlan("Di bo");
        treatmentPlan.setGlucoseMonitoringPlan("Do hang ngay");
        treatmentPlan.setCreatedAt(LocalDateTime.of(2026, 7, 22, 12, 0));

        clinicalExam = new ClinicalExamination();
        clinicalExam.setClinicalExamId("CE-01");
        clinicalExam.setExamDate(LocalDateTime.of(2026, 7, 22, 10, 0));
        clinicalExam.setPatient(patient);
        clinicalExam.setTreatmentPlan(treatmentPlan);
        clinicalExam.setDiagnosisNote("Tieu duong type 2");
        clinicalExam.setNextAppointment(LocalDateTime.of(2026, 8, 22, 10, 0));

        LabTestCatalog testCatalog = new LabTestCatalog();
        testCatalog.setTestName("Glucose");
        testCatalog.setUnit("mmol/L");

        LabOrder order = new LabOrder();
        order.setClinicalExamination(clinicalExam);

        labResult = new LabResult();
        labResult.setLabTest(testCatalog);
        labResult.setResultValue(java.math.BigDecimal.valueOf(7.5));
        labResult.setReferenceRange("4.1-5.6");
        labResult.setFlag("Cao");
        labResult.setLabOrder(order);

        Medication medication = new Medication();
        medication.setMedicationName("Metformin");
        medication.setForm("tablet");
        medication.setAdministrationRoute("oral");
        medication.setUsageInstruction("Uong sau an");

        Prescription prescription = new Prescription();
        prescription.setClinicalExamination(clinicalExam);

        prescriptionDetail = new PrescriptionDetail();
        prescriptionDetail.setPrescription(prescription);
        prescriptionDetail.setMedication(medication);
        prescriptionDetail.setDosage("1 tablet/lần");
        prescriptionDetail.setStartDate(LocalDate.of(2026, 7, 22));
        prescriptionDetail.setEndDate(LocalDate.of(2026, 8, 22));
        prescriptionDetail.setMedicationPlan("1 thang");

        PrescriptionTiming timing = new PrescriptionTiming();
        MedicationTiming t = new MedicationTiming();
        t.setTimingName("Sang");
        timing.setTiming(t);
        prescriptionDetail.setPrescriptionTimings(List.of(timing));
    }

    @Test
    void testGetGeneralRecord_Success() {
        when(patientRepository.findById("PAT-01")).thenReturn(Optional.of(patient));
        String res = aiTool.getGeneralRecord("PAT-01");
        assertTrue(res.contains("Nguyen Van A"));
        assertTrue(res.contains("Nam"));

        // test female gender
        patient.setGender(false);
        String resFemale = aiTool.getGeneralRecord("PAT-01");
        assertTrue(resFemale.contains("Nữ"));

        // test null gender
        patient.setGender(null);
        String resNull = aiTool.getGeneralRecord("PAT-01");
        assertTrue(resNull.contains("Chưa có thông tin"));
    }

    @Test
    void testGetGeneralRecord_Empty() {
        when(patientRepository.findById("PAT-99")).thenReturn(Optional.empty());
        String res = aiTool.getGeneralRecord("PAT-99");
        assertTrue(res.contains("Không có dữ liệu"));
    }

    @Test
    void testGetClinicalExamination_Success() {
        when(clinicalExaminationRepository.findFirstByPatient_UserIdOrderByExamDateDesc("PAT-01"))
                .thenReturn(Optional.of(clinicalExam));
        when(examSymptomRepository.findSymptomNamesByClinicalExamId("CE-01"))
                .thenReturn(List.of("Khat nuoc"));
        when(labResultRepository.findByPatientIdWithDetails("PAT-01"))
                .thenReturn(List.of(labResult));
        when(prescriptionDetailRepository.findByPatientIdWithDetails("PAT-01"))
                .thenReturn(List.of(prescriptionDetail));

        String res = aiTool.getClinicalExamination("PAT-01");
        assertTrue(res.contains("Tieu duong type 2"));
        assertTrue(res.contains("Khat nuoc"));
        assertTrue(res.contains("Glucose"));
        assertTrue(res.contains("Metformin"));
    }

    @Test
    void testGetClinicalExamination_Empty() {
        when(clinicalExaminationRepository.findFirstByPatient_UserIdOrderByExamDateDesc("PAT-01"))
                .thenReturn(Optional.empty());
        String res = aiTool.getClinicalExamination("PAT-01");
        assertTrue(res.contains("Không có dữ liệu"));
    }

    @Test
    void testGetTreatmentPlan_Success() {
        when(clinicalExaminationRepository.findByPatient_UserIdOrderByExamDateDesc("PAT-01"))
                .thenReturn(List.of(clinicalExam));

        String res = aiTool.getTreatmentPlan("PAT-01");
        assertTrue(res.contains("Giam duong huyet"));
        assertTrue(res.contains("It tinh bot"));
    }

    @Test
    void testGetTreatmentPlan_Fallback() {
        clinicalExam.setTreatmentPlan(null); // trigger fallback values
        when(clinicalExaminationRepository.findByPatient_UserIdOrderByExamDateDesc("PAT-01"))
                .thenReturn(List.of(clinicalExam));

        String res = aiTool.getTreatmentPlan("PAT-01");
        assertTrue(res.contains("Duy trì đường huyết"));
    }

    @Test
    void testGetTreatmentPlan_Empty() {
        when(clinicalExaminationRepository.findByPatient_UserIdOrderByExamDateDesc("PAT-01"))
                .thenReturn(Collections.emptyList());
        String res = aiTool.getTreatmentPlan("PAT-01");
        assertTrue(res.contains("Không có dữ liệu"));
    }

    @Test
    void testGetLabResults_Success() {
        when(labResultRepository.findByPatientIdWithDetails("PAT-01")).thenReturn(List.of(labResult));
        String res = aiTool.getLabResults("PAT-01");
        assertTrue(res.contains("Glucose"));
        assertTrue(res.contains("7.5"));
    }

    @Test
    void testGetLabResults_EmptyList() {
        when(labResultRepository.findByPatientIdWithDetails("PAT-01")).thenReturn(Collections.emptyList());
        String res = aiTool.getLabResults("PAT-01");
        assertTrue(res.contains("Không có dữ liệu"));
    }

    @Test
    void testGetLabResults_NullExamDate() {
        LabTestCatalog catalog = new LabTestCatalog();
        catalog.setTestName("HbA1c");
        catalog.setUnit("%");

        LabOrder orderNullDate = new LabOrder();
        ClinicalExamination ceNullDate = new ClinicalExamination();
        ceNullDate.setExamDate(null);
        orderNullDate.setClinicalExamination(ceNullDate);

        LabResult lrNullDate = new LabResult();
        lrNullDate.setLabTest(catalog);
        lrNullDate.setResultValue(java.math.BigDecimal.valueOf(6.8));
        lrNullDate.setReferenceRange("< 6.5");
        lrNullDate.setFlag("Cao");
        lrNullDate.setLabOrder(orderNullDate);

        when(labResultRepository.findByPatientIdWithDetails("PAT-01")).thenReturn(List.of(lrNullDate));
        String res = aiTool.getLabResults("PAT-01");
        assertTrue(res.contains("HbA1c"));
        assertTrue(res.contains("6.8"));
    }


    @Test
    void testGetPrescriptions_Success() {
        when(clinicalExaminationRepository.findByPatient_UserIdOrderByExamDateDesc("PAT-01"))
                .thenReturn(List.of(clinicalExam));
        when(prescriptionDetailRepository.findByClinicalExamIdWithDetails("CE-01"))
                .thenReturn(List.of(prescriptionDetail));

        String res = aiTool.getPrescriptions("PAT-01");
        assertTrue(res.contains("Metformin"));
        assertTrue(res.contains("viên/lần"));
    }

    @Test
    void testGetPrescriptions_Empty() {
        when(clinicalExaminationRepository.findByPatient_UserIdOrderByExamDateDesc("PAT-01"))
                .thenReturn(List.of(clinicalExam));
        when(prescriptionDetailRepository.findByClinicalExamIdWithDetails("CE-01"))
                .thenReturn(Collections.emptyList());

        String res = aiTool.getPrescriptions("PAT-01");
        assertTrue(res.contains("chưa có thuốc nào được kê"));
    }

    @Test
    void testGetPrescriptions_NoExams() {
        when(clinicalExaminationRepository.findByPatient_UserIdOrderByExamDateDesc("PAT-01"))
                .thenReturn(Collections.emptyList());

        String res = aiTool.getPrescriptions("PAT-01");
        assertTrue(res.contains("chưa có lượt khám nào"));
    }

    @Test
    void testTranslateKey_AllCases() {
        String[] keys = {
            "fullName", "gender", "height", "weight", "bloodgroup",
            "permanentMedicalHistory", "allergyNotes", "examDate", "doctorName",
            "diagnosisNote", "nextAppointment", "symptoms", "labResults",
            "prescriptions", "treatmentPlan", "status", "cancelReason",
            "treatmentGoal", "dietPlan", "exercisePlan", "glucoseMonitoringPlan",
            "createdAt", "testName", "resultValue", "referenceRange", "unit",
            "flag", "patientId", "clinicalExamId", "medicationName", "dosage",
            "startDate", "endDate", "form", "administrationRoute",
            "usageInstruction", "timings", "medicationPlan"
        };
        for (String k : keys) {
            String val = ReflectionTestUtils.invokeMethod(aiTool, "translateKey", k);
            assertNotNull(val);
            assertNotEquals(k, val);
        }
        // test default case
        String def = ReflectionTestUtils.invokeMethod(aiTool, "translateKey", "someRandomKey");
        assertEquals("Some Random Key", def);
    }

    @Test
    void testFormatResult_MultipleRecords() {
        List<Map<String, Object>> list = List.of(
            Map.of("fullName", "Nguyen Van A"),
            Map.of("fullName", "Tran Thi B")
        );
        String res = ReflectionTestUtils.invokeMethod(aiTool, "formatResult", list, "Danh sách");
        assertTrue(res.contains("Bản ghi 1"));
        assertTrue(res.contains("Bản ghi 2"));
    }

    @Test
    void testFormatResult_JacksonException() {
        class FailObj {
            public String getFullName() {
                throw new RuntimeException("Jackson error");
            }
        }
        List<FailObj> list = List.of(new FailObj());
        String res = ReflectionTestUtils.invokeMethod(aiTool, "formatResult", list, "Lỗi");
        assertTrue(res.contains("Lỗi khi định dạng dữ liệu"));
    }

    @Test
    void testFormatFriendlyValue_SpecialDates() {
        String res = ReflectionTestUtils.invokeMethod(aiTool, "formatFriendlyValue", "examDate", "2026-07-22T00:00:00");
        assertEquals("Ngày 22/07/2026", res);

        String res2 = ReflectionTestUtils.invokeMethod(aiTool, "formatFriendlyValue", "examDate", "2026-07-22T10:30:00");
        assertEquals("22/07/2026 (10:30)", res2);
    }

    @Test
    void testFormatFriendlyValue_ParseExceptions() {
        String res = ReflectionTestUtils.invokeMethod(aiTool, "formatFriendlyValue", "examDate", "2026-99-99T99:99");
        assertEquals("2026-99-99T99:99", res);

        String res2 = ReflectionTestUtils.invokeMethod(aiTool, "formatFriendlyValue", "examDate", "2026-99-99");
        assertEquals("2026-99-99", res2);
    }

    @Test
    void testGetPrescriptions_FallbackToPreviousExam() {
        ClinicalExamination ce1 = new ClinicalExamination();
        ce1.setClinicalExamId("CE-01");
        ClinicalExamination ce2 = new ClinicalExamination();
        ce2.setClinicalExamId("CE-02");

        when(clinicalExaminationRepository.findByPatient_UserIdOrderByExamDateDesc("PAT-01"))
                .thenReturn(List.of(ce1, ce2));

        // First exam has no details
        when(prescriptionDetailRepository.findByClinicalExamIdWithDetails("CE-01"))
                .thenReturn(Collections.emptyList());
        // Second exam has details
        when(prescriptionDetailRepository.findByClinicalExamIdWithDetails("CE-02"))
                .thenReturn(List.of(prescriptionDetail));

        String res = aiTool.getPrescriptions("PAT-01");
        assertTrue(res.contains("Metformin"));
    }

    @Test
    void testGetClinicalExamination_FallbackMocks() {
        // Set doctor with profile to cover doctor name branch
        User doc = new User();
        Profile prof = new Profile();
        prof.setFullName("Dr. House");
        doc.setProfile(prof);
        clinicalExam.setDoctor(doc);

        // Test with prescription details having null relations to cover ternary branches
        PrescriptionDetail nullRelDetail1 = new PrescriptionDetail();
        nullRelDetail1.setPrescription(null); // pd.getPrescription() == null
        nullRelDetail1.setMedication(prescriptionDetail.getMedication());

        PrescriptionDetail nullRelDetail2 = new PrescriptionDetail();
        Prescription p2 = new Prescription();
        p2.setClinicalExamination(null); // pd.getPrescription().getClinicalExamination() == null
        nullRelDetail2.setPrescription(p2);
        nullRelDetail2.setMedication(prescriptionDetail.getMedication());

        PrescriptionDetail nullRelDetail3 = new PrescriptionDetail();
        Prescription p3 = new Prescription();
        ClinicalExamination ce3 = new ClinicalExamination();
        ce3.setPatient(null); // pd.getPrescription().getClinicalExamination().getPatient() == null
        p3.setClinicalExamination(ce3);
        nullRelDetail3.setPrescription(p3);
        nullRelDetail3.setMedication(prescriptionDetail.getMedication());

        when(clinicalExaminationRepository.findFirstByPatient_UserIdOrderByExamDateDesc("PAT-01"))
                .thenReturn(Optional.of(clinicalExam));
        when(examSymptomRepository.findSymptomNamesByClinicalExamId("CE-01"))
                .thenReturn(List.of("Khat nuoc"));
        when(labResultRepository.findByPatientIdWithDetails("PAT-01"))
                .thenReturn(List.of(labResult));
        when(prescriptionDetailRepository.findByPatientIdWithDetails("PAT-01"))
                .thenReturn(List.of(nullRelDetail1, nullRelDetail2, nullRelDetail3));

        String res = aiTool.getClinicalExamination("PAT-01");
        assertTrue(res.contains("Dr. House"));
        assertTrue(res.contains("Metformin"));
    }

    @Test
    void testAiToolImpl_ExtraBranchCoverage() {
        // 1. translateMedicalTerms(null)
        String nullTerms = ReflectionTestUtils.invokeMethod(aiTool, "translateMedicalTerms", (Object) null);
        assertEquals("", nullTerms);

        // 2. formatResult(null, "header") and formatResult(emptyList, "header")
        String nullRes = ReflectionTestUtils.invokeMethod(aiTool, "formatResult", null, "Header");
        assertTrue(nullRes.contains("Không có dữ liệu"));
        String emptyRes = ReflectionTestUtils.invokeMethod(aiTool, "formatResult", List.of(), "Header");
        assertTrue(emptyRes.contains("Không có dữ liệu"));

        // 3. formatMapToText and formatFriendlyValue extra branches
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("gender", "Nam");
        map.put("height", "");
        map.put("weight", "Nữ");
        map.put("bloodgroup", "true");
        map.put("permanentMedicalHistory", "false");
        map.put("examDate", null);
        map.put("nextAppointment", "2026-07-22");
        map.put("symptoms", List.of());
        map.put("allergyNotes", "[]"); // triggers "[]" equals branch in line 350
        
        // Custom List that overrides toString and isEmpty to cover line 364 branch
        List<Object> customList = new java.util.ArrayList<>() {
            @Override
            public String toString() {
                return "NotEmptyString";
            }
            @Override
            public boolean isEmpty() {
                return true;
            }
        };
        map.put("customList", customList);

        String mapRes = ReflectionTestUtils.invokeMethod(aiTool, "formatMapToText", map, 0);
        assertNotNull(mapRes);

        // formatFriendlyValue values check
        String f1 = ReflectionTestUtils.invokeMethod(aiTool, "formatFriendlyValue", "key", null);
        assertEquals("Chưa có thông tin", f1);
        String f2 = ReflectionTestUtils.invokeMethod(aiTool, "formatFriendlyValue", "Giới tính", "true");
        assertEquals("Nam", f2);
        String f2a = ReflectionTestUtils.invokeMethod(aiTool, "formatFriendlyValue", "Giới tính", "Nam");
        assertEquals("Nam", f2a);
        String f3 = ReflectionTestUtils.invokeMethod(aiTool, "formatFriendlyValue", "Giới tính", "false");
        assertEquals("Nữ", f3);
        String f3a = ReflectionTestUtils.invokeMethod(aiTool, "formatFriendlyValue", "Giới tính", "Nữ");
        assertEquals("Nữ", f3a);
        String f4 = ReflectionTestUtils.invokeMethod(aiTool, "formatFriendlyValue", "examDate", "2026-07-22");
        assertEquals("22/07/2026", f4);
        String f4a = ReflectionTestUtils.invokeMethod(aiTool, "formatFriendlyValue", "examDate", "2026-07-22T10:30:00.123456");
        assertEquals("22/07/2026 (10:30)", f4a);

        // 4. getTreatmentPlan fallback details
        ClinicalExamination ceNoPlan = new ClinicalExamination();
        ceNoPlan.setClinicalExamId("CE-02");
        ceNoPlan.setExamDate(null); // latestWithPlan.getExamDate() == null
        ceNoPlan.setDiagnosisNote(null); // latestWithPlan.getDiagnosisNote() is null
        
        TreatmentPlan emptyPlan = new TreatmentPlan(); // all fields null to trigger default fallback texts
        ceNoPlan.setTreatmentPlan(emptyPlan);

        ClinicalExamination ceBlankNote = new ClinicalExamination();
        ceBlankNote.setClinicalExamId("CE-02b");
        ceBlankNote.setExamDate(null);
        ceBlankNote.setDiagnosisNote(""); // blank but not null
        ceBlankNote.setTreatmentPlan(emptyPlan);
        
        when(clinicalExaminationRepository.findByPatient_UserIdOrderByExamDateDesc("PAT-01"))
                .thenReturn(List.of(ceNoPlan));
        String planRes = aiTool.getTreatmentPlan("PAT-01");
        assertNotNull(planRes);
        assertTrue(planRes.contains("Theo dõi và duy trì"));

        when(clinicalExaminationRepository.findByPatient_UserIdOrderByExamDateDesc("PAT-01"))
                .thenReturn(List.of(ceBlankNote));
        String planRes2 = aiTool.getTreatmentPlan("PAT-01");
        assertNotNull(planRes2);
        assertTrue(planRes2.contains("Theo dõi và duy trì"));

        // exams null branch in getTreatmentPlan
        when(clinicalExaminationRepository.findByPatient_UserIdOrderByExamDateDesc("PAT-01"))
                .thenReturn(null);
        String planNullExams = aiTool.getTreatmentPlan("PAT-01");
        assertTrue(planNullExams.contains("Không có dữ liệu"));

        // 5. getClinicalExamination missing doctor details
        ClinicalExamination ceNoDoctor = new ClinicalExamination();
        ceNoDoctor.setClinicalExamId("CE-03");
        ceNoDoctor.setDoctor(null); // doctor is null
        
        // timings empty check for getClinicalExamination
        PrescriptionDetail emptyTimingsDetail = new PrescriptionDetail();
        emptyTimingsDetail.setPrescription(null);
        emptyTimingsDetail.setPrescriptionTimings(List.of());
        Medication mNulls = new Medication();
        mNulls.setMedicationName(null);
        mNulls.setForm(null);
        mNulls.setAdministrationRoute(null);
        mNulls.setUsageInstruction(null);
        emptyTimingsDetail.setMedication(mNulls);

        when(clinicalExaminationRepository.findFirstByPatient_UserIdOrderByExamDateDesc("PAT-01"))
                .thenReturn(Optional.of(ceNoDoctor));
        when(prescriptionDetailRepository.findByPatientIdWithDetails("PAT-01"))
                .thenReturn(List.of(emptyTimingsDetail));
        String examRes = aiTool.getClinicalExamination("PAT-01");
        assertNotNull(examRes);
        assertFalse(examRes.contains("Bác sĩ:"));

        // Doctor with null profile
        User docNullProfile = new User();
        docNullProfile.setProfile(null);
        ceNoDoctor.setDoctor(docNullProfile);
        
        when(clinicalExaminationRepository.findFirstByPatient_UserIdOrderByExamDateDesc("PAT-01"))
                .thenReturn(Optional.of(ceNoDoctor));
        String examRes2 = aiTool.getClinicalExamination("PAT-01");
        assertNotNull(examRes2);

        // 6. getPrescriptions null and empty exam list branches
        when(clinicalExaminationRepository.findByPatient_UserIdOrderByExamDateDesc("PAT-01"))
                .thenReturn(null);
        String pres1 = aiTool.getPrescriptions("PAT-01");
        assertTrue(pres1.contains("chưa có lượt khám nào"));

        when(clinicalExaminationRepository.findByPatient_UserIdOrderByExamDateDesc("PAT-01"))
                .thenReturn(List.of());
        String pres2 = aiTool.getPrescriptions("PAT-01");
        assertTrue(pres2.contains("chưa có lượt khám nào"));

        // Multiple exams but none have details (natural exit of loop)
        ClinicalExamination ce1 = new ClinicalExamination();
        ce1.setClinicalExamId("CE-11");
        ClinicalExamination ce2 = new ClinicalExamination();
        ce2.setClinicalExamId("CE-12");
        when(clinicalExaminationRepository.findByPatient_UserIdOrderByExamDateDesc("PAT-01"))
                .thenReturn(List.of(ce1, ce2));
        when(prescriptionDetailRepository.findByClinicalExamIdWithDetails(anyString()))
                .thenReturn(Collections.emptyList());
        String pres3 = aiTool.getPrescriptions("PAT-01");
        assertTrue(pres3.contains("chưa có thuốc nào được kê"));

        // 7. Prescription timings and null mappings in getPrescriptions
        ClinicalExamination cePres = new ClinicalExamination();
        cePres.setClinicalExamId("CE-04");
        cePres.setPatient(null);
        
        Prescription p = new Prescription();
        p.setClinicalExamination(cePres);
        
        PrescriptionDetail detailNulls = new PrescriptionDetail();
        detailNulls.setPrescription(p);
        detailNulls.setPrescriptionTimings(null); // pd.getPrescriptionTimings() == null
        Medication mNulls1 = new Medication();
        mNulls1.setMedicationName(null);
        mNulls1.setForm(null);
        mNulls1.setAdministrationRoute(null);
        mNulls1.setUsageInstruction(null);
        detailNulls.setMedication(mNulls1);
        detailNulls.setDosage(null);
        detailNulls.setStartDate(null);
        detailNulls.setEndDate(LocalDate.of(2026, 8, 22)); // end date not null

        PrescriptionDetail detailEmptyTimings = new PrescriptionDetail();
        detailEmptyTimings.setPrescription(p);
        detailEmptyTimings.setPrescriptionTimings(List.of()); // pd.getPrescriptionTimings().isEmpty()
        Medication mNulls2 = new Medication();
        mNulls2.setMedicationName(null);
        mNulls2.setForm(null);
        mNulls2.setAdministrationRoute(null);
        mNulls2.setUsageInstruction(null);
        detailEmptyTimings.setMedication(mNulls2);
        detailEmptyTimings.setStartDate(LocalDate.of(2026, 7, 22));

        // Blank timing name check
        PrescriptionTiming blankTiming = new PrescriptionTiming();
        MedicationTiming tBlank = new MedicationTiming();
        tBlank.setTimingName("  "); // blank TimingName
        blankTiming.setTiming(tBlank);
        
        PrescriptionDetail detailBlankTiming = new PrescriptionDetail();
        detailBlankTiming.setPrescription(p);
        detailBlankTiming.setPrescriptionTimings(List.of(blankTiming));
        Medication mBlankUsage = new Medication();
        mBlankUsage.setMedicationName(null);
        mBlankUsage.setForm(null);
        mBlankUsage.setAdministrationRoute(null);
        mBlankUsage.setUsageInstruction("  "); // blank UsageInstruction
        detailBlankTiming.setMedication(mBlankUsage);

        // Prescription detail null relations
        PrescriptionDetail detailNoPrescription = new PrescriptionDetail();
        detailNoPrescription.setPrescription(null);
        Medication mNulls3 = new Medication();
        mNulls3.setMedicationName(null);
        mNulls3.setForm(null);
        mNulls3.setAdministrationRoute(null);
        mNulls3.setUsageInstruction(null);
        detailNoPrescription.setMedication(mNulls3);

        PrescriptionDetail detailNoExam = new PrescriptionDetail();
        Prescription pNoExam = new Prescription();
        pNoExam.setClinicalExamination(null);
        detailNoExam.setPrescription(pNoExam);
        Medication mNulls4 = new Medication();
        mNulls4.setMedicationName(null);
        mNulls4.setForm(null);
        mNulls4.setAdministrationRoute(null);
        mNulls4.setUsageInstruction(null);
        detailNoExam.setMedication(mNulls4);

        when(clinicalExaminationRepository.findByPatient_UserIdOrderByExamDateDesc("PAT-01"))
                .thenReturn(List.of(cePres));
        when(prescriptionDetailRepository.findByClinicalExamIdWithDetails("CE-04"))
                .thenReturn(List.of(detailNulls, detailEmptyTimings, detailBlankTiming, detailNoPrescription, detailNoExam));

        String presResult = aiTool.getPrescriptions("PAT-01");
        assertNotNull(presResult);
    }
}
