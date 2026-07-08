package com.quan.diabetes.dto.admin;

public class DashboardStatsDTO {
    private long totalPatients;
    private long totalDoctors;
    private long totalConversations;
    private long totalReminders;
    
    private long highRiskPatients; 
    private long abnormalGlucoseAlerts;

    public DashboardStatsDTO() {}

    public DashboardStatsDTO(long totalPatients, long totalDoctors, long totalConversations, long totalReminders, long highRiskPatients, long abnormalGlucoseAlerts) {
        this.totalPatients = totalPatients;
        this.totalDoctors = totalDoctors;
        this.totalConversations = totalConversations;
        this.totalReminders = totalReminders;
        this.highRiskPatients = highRiskPatients;
        this.abnormalGlucoseAlerts = abnormalGlucoseAlerts;
    }

    public long getTotalPatients() { return totalPatients; }
    public void setTotalPatients(long totalPatients) { this.totalPatients = totalPatients; }

    public long getTotalDoctors() { return totalDoctors; }
    public void setTotalDoctors(long totalDoctors) { this.totalDoctors = totalDoctors; }

    public long getTotalConversations() { return totalConversations; }
    public void setTotalConversations(long totalConversations) { this.totalConversations = totalConversations; }

    public long getTotalReminders() { return totalReminders; }
    public void setTotalReminders(long totalReminders) { this.totalReminders = totalReminders; }

    public long getHighRiskPatients() { return highRiskPatients; }
    public void setHighRiskPatients(long highRiskPatients) { this.highRiskPatients = highRiskPatients; }

    public long getAbnormalGlucoseAlerts() { return abnormalGlucoseAlerts; }
    public void setAbnormalGlucoseAlerts(long abnormalGlucoseAlerts) { this.abnormalGlucoseAlerts = abnormalGlucoseAlerts; }
}
