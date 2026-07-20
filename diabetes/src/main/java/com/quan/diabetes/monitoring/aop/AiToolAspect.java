package com.quan.diabetes.monitoring.aop;

import com.quan.diabetes.monitoring.context.AiRequestContextHolder;
import com.quan.diabetes.monitoring.entity.AiPatientAccessLog;
import com.quan.diabetes.monitoring.service.AiMonitoringService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
public class AiToolAspect {

    private static final Logger logger = LoggerFactory.getLogger(AiToolAspect.class);

    @Autowired
    private AiMonitoringService aiMonitoringService;

    @Around("execution(* com.quan.diabetes.service.ai.AiTool.*(..))")
    public Object logPatientAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = null;
        try {
            result = joinPoint.proceed();
        } finally {
            try {
                Object[] args = joinPoint.getArgs();
                if (args != null && args.length > 0 && args[0] instanceof String) {
                    String patientId = (String) args[0];
                    String methodName = joinPoint.getSignature().getName();
                    String dataType = mapMethodNameToDataType(methodName);
                    String question = AiRequestContextHolder.getCurrentQuestion();
                    long latency = System.currentTimeMillis() - start;

                    AiPatientAccessLog accessLog = new AiPatientAccessLog(
                            null,
                            patientId,
                            dataType,
                            LocalDateTime.now(),
                            question,
                            latency
                    );
                    aiMonitoringService.logPatientAccess(accessLog);
                }
            } catch (Exception e) {
                logger.error("Error logging AI Patient Access in AOP: {}", e.getMessage(), e);
            }
        }
        return result;
    }

    private String mapMethodNameToDataType(String methodName) {
        switch (methodName) {
            case "getGeneralRecord": return "get_general_record";
            case "getClinicalExamination": return "get_clinical_examination";
            case "getTreatmentPlan": return "get_treatment_plan";
            case "getLabResults": return "get_lab_results";
            case "getPrescriptions": return "get_prescriptions";
            default: return methodName.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
        }
    }
}
