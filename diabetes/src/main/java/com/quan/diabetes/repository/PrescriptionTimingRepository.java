package com.quan.diabetes.repository;

import com.quan.diabetes.entity.PrescriptionTiming;
import com.quan.diabetes.dto.reminder.PrescriptionReminderDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionTimingRepository
        extends JpaRepository<PrescriptionTiming, Long> {
    List<PrescriptionTiming>
    findByPrescriptionDetail_PrescriptionDetailId(
            String prescriptionDetailID
    );

    void deleteByPrescriptionDetail_PrescriptionDetailId(
            String prescriptionDetailID
    );

    boolean existsByPrescriptionDetail_PrescriptionDetailIdAndTiming_TimingID(
            String prescriptionDetailID,
            Integer timingID
    );

    @Query("SELECT new com.quan.diabetes.dto.reminder.PrescriptionReminderDto("
            + "p.clinicalExamination.patient.userId, "
            + "p.clinicalExamination.clinicalExamId, "
            + "pd.medication.medicationName, "
            + "pd.dosage, "
            + "pd.startDate, "
            + "pd.endDate, "
            + "pd.medication.form, "
            + "pd.medication.administrationRoute, "
            + "pd.medication.usageInstruction, "
            + "mt.timingName, "
            + "pd.medicationPlan, "
            + "p.clinicalExamination.treatmentPlan) "
            + "FROM PrescriptionTiming pt "
            + "JOIN pt.prescriptionDetail pd "
            + "JOIN pd.prescription p "
            + "JOIN pt.timing mt "
            + "WHERE p.clinicalExamination.clinicalExamId = :clinicalExamId")
    List<PrescriptionReminderDto> findDescriptionRemindersByClinicalExamId(
            @Param("clinicalExamId") String clinicalExamId
    );
}
