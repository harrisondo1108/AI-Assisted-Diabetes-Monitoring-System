package com.quan.diabetes.service.admin.impl;

import com.quan.diabetes.dto.admin.DashboardStatsDTO;
import com.quan.diabetes.repository.AIConversationRepository;
import com.quan.diabetes.repository.ClinicalExaminationRepository;
import com.quan.diabetes.repository.ReminderRepository;
import com.quan.diabetes.service.admin.DashboardService;
import com.quan.diabetes.service.user.PatientService;
import com.quan.diabetes.service.user.ProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final PatientService patientService;
    private final ProfileService doctorService;
    private final AIConversationRepository aiConversationRepository;
    private final ReminderRepository reminderRepository;
    private final ClinicalExaminationRepository clinicalExaminationRepository;

    public DashboardServiceImpl(
            PatientService patientService,
            ProfileService doctorService,
            AIConversationRepository aiConversationRepository,
            ReminderRepository reminderRepository,
            ClinicalExaminationRepository clinicalExaminationRepository) {
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.aiConversationRepository = aiConversationRepository;
        this.reminderRepository = reminderRepository;
        this.clinicalExaminationRepository = clinicalExaminationRepository;
    }

    @Override
    public DashboardStatsDTO getDashboardStats() {
        long totalPatients = patientService.findAll().size();
        long totalDoctors = doctorService.findTotalDoctor().size();
        long totalConversations = aiConversationRepository.count();
        long totalReminders = reminderRepository.count();

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        long todayConversationsCount = aiConversationRepository.findByCreatedAtBetween(startOfDay, endOfDay).size();
        long todayRemindersCount = reminderRepository.findByScheduledTimeBetween(startOfDay, endOfDay).size();
        long todayCompletedExamsCount = clinicalExaminationRepository.findByExamDateBetweenAndStatus(startOfDay, endOfDay, "Completed").size();

        DashboardStatsDTO dto = new DashboardStatsDTO(
                totalPatients,
                totalDoctors,
                totalConversations,
                totalReminders,
                0, // Placeholder for highRiskPatients
                0  // Placeholder for abnormalGlucoseAlerts
        );
        dto.setTodayChats(todayConversationsCount);
        dto.setTodayReminders(todayRemindersCount);
        dto.setTodayCompletedExams(todayCompletedExamsCount);
        
        return dto;
    }
}
