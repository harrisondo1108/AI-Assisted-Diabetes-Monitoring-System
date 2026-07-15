package com.quan.diabetes.service.exam;

import com.quan.diabetes.dto.doctor.ClinicalExamForm;
import com.quan.diabetes.dto.doctor.ExamStep1Form;
import com.quan.diabetes.dto.doctor.ExamStep2Form;
import com.quan.diabetes.dto.doctor.ExamStep3Form;
import com.quan.diabetes.dto.doctor.PrescriptionLineDTO;
import com.quan.diabetes.entity.ClinicalExamination;
import com.quan.diabetes.entity.LabTestCatalog;
import com.quan.diabetes.entity.PatientType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface ClinicalExaminationService {

    public List<ClinicalExamination> findAll();

    public Optional<ClinicalExamination> findById(String id);

    public ClinicalExamination create(ClinicalExamination entity);

    public ClinicalExamination update(String id, ClinicalExamination entity);

    public void deleteById(String id);

    public boolean existsById(String id);

    List<ClinicalExamination> findByDoctorId(String doctorId);

    List<ClinicalExamination> findByPatientId(String patientId);

    void cancelExamination(String patientId, String reason, String doctorId);

    void startExamination(String patientId, String doctorId);

    void createAutoPendingExamination(String patientId);

    void requestExamination(String patientId, String medicalHistory);

    List<PrescriptionLineDTO> getPrescriptionLines(String examId);

    // ---- Legacy methods (kept for compatibility) ----
    void submitExamination(String patientId, ClinicalExamForm form, String doctorId);
    void updateExamination(String examId, ClinicalExamForm form);
    void saveDraft(String patientId, ClinicalExamForm form, String doctorId);

    // ---- Tab-based step methods ----

    /** Save Tab 1 data: medical history + symptoms */
    void saveStep1(String examId, ExamStep1Form form);

    /** Save Tab 2 data: pregnancy flag; optionally create/replace all lab orders */
    void saveStep2(String examId, ExamStep2Form form, PatientType matchedType, List<LabTestCatalog> testCatalog);

    /** Save Tab 3 data: diagnosis note, next appointment, treatment plan */
    void saveStep3(String examId, ExamStep3Form form);

    /**
     * Save prescription lines from session to DB and mark examination as Completed.
     * Also triggers reminder generation.
     */
    void completeExamination(String examId, List<PrescriptionLineDTO> prescriptionLines);
}
