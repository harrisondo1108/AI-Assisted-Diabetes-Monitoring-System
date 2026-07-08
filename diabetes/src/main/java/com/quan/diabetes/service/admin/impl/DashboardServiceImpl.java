package com.quan.diabetes.service.admin.impl;

import com.quan.diabetes.dto.admin.DashboardStatsDTO;
import com.quan.diabetes.repository.AIConversationRepository;
import com.quan.diabetes.repository.AIReminderRepository;
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
    private final AIReminderRepository aiReminderRepository;

    public DashboardServiceImpl(
            PatientService patientService,
            ProfileService doctorService,
            AIConversationRepository aiConversationRepository,
            AIReminderRepository aiReminderRepository) {
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.aiConversationRepository = aiConversationRepository;
        this.aiReminderRepository = aiReminderRepository;
    }

    @Override
    public DashboardStatsDTO getDashboardStats() {
        long totalPatients = patientService.findAll().size();
        long totalDoctors = doctorService.findTotalDoctor().size();
        long totalConversations = aiConversationRepository.count();
        long totalReminders = aiReminderRepository.count();

        return new DashboardStatsDTO(
                totalPatients,
                totalDoctors,
                totalConversations,
                totalReminders,
                0, // Placeholder for highRiskPatients
                0  // Placeholder for abnormalGlucoseAlerts
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> getConversationCountsLast7Days() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        List<com.quan.diabetes.entity.AIConversation> list = aiConversationRepository.findByCreatedAtBetween(start, end);

        Map<LocalDate, Long> counts = list.stream()
                .filter(c -> c.getCreatedAt() != null)
                .collect(Collectors.groupingBy(c -> c.getCreatedAt().toLocalDate(), Collectors.counting()));

        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = startDate.plusDays(i);
            result.add(counts.getOrDefault(d, 0L).intValue());
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> getReminderCountsLast7Days() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        List<com.quan.diabetes.entity.AIReminder> list = aiReminderRepository.findByScheduledTimeBetween(start, end);

        Map<LocalDate, Long> counts = list.stream()
                .filter(r -> r.getScheduledTime() != null)
                .collect(Collectors.groupingBy(r -> r.getScheduledTime().toLocalDate(), Collectors.counting()));

        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = startDate.plusDays(i);
            result.add(counts.getOrDefault(d, 0L).intValue());
        }
        return result;
    }
}
