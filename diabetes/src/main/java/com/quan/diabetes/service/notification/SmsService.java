package com.quan.diabetes.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class SmsService {
    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    @Value("${speedsms.api.url}")
    private String apiUrl;

    @Value("${speedsms.access.token}")
    private String accessToken;

    @Value("${speedsms.type}")
    private int smsType;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendOtp(String phoneNumber, String otp) {
        String normalizedPhone = normalizePhoneNumber(phoneNumber);
        String messageContent = "Ma OTP cua ban la " + otp + ". Vui long khong chia se ma nay voi ai.";

        try {
            // Encode basic auth credential (access_token + ":")
            String credential = Base64.getEncoder().encodeToString((accessToken + ":").getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + credential);
            headers.set("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36");

            // Construct payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("to", new String[] { normalizedPhone });
            payload.put("content", messageContent);
            payload.put("sms_type", smsType);
            payload.put("sender", "");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            logger.info("Sending OTP SMS to phone: {} (Normalized: {})", phoneNumber, normalizedPhone);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            logger.info("SpeedSMS API response: {}", response.getBody());
        } catch (org.springframework.web.client.HttpClientErrorException.Forbidden e) {
            logger.warn("=== LƯU Ý: API SpeedSMS bị Cloudflare chặn ở môi trường Local (403 Forbidden) ===");
            logger.warn("Chi tiết: Cloudflare yêu cầu giải mã captcha/javascript khi gọi từ IP mạng cá nhân.");
            logger.warn(
                    "Giải pháp test nhanh: Hãy lấy mã OTP [{}] đã được in ở Console Log để nhập và kiểm thử tiếp tục luồng giao diện.",
                    otp);
        } catch (Exception e) {
            logger.error("Failed to send OTP SMS to {}: {}", phoneNumber, e.getMessage(), e);
        }
    }

    private String normalizePhoneNumber(String phone) {
        if (phone == null)
            return "";
        String clean = phone.replaceAll("[^0-9]", "");
        if (clean.startsWith("0")) {
            return "84" + clean.substring(1);
        }
        return clean;
    }
}
