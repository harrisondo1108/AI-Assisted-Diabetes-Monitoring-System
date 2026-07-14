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

    @Value("${speedsms.sender:}")
    private String smsSender;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendOtp(String phoneNumber, String otp) {
        String normalizedPhone = normalizePhoneNumber(phoneNumber);
        String messageContent = "Ma OTP cua ban la " + otp + ". Vui long khong chia se ma nay voi ai.";

        try {
            // Trim token để loại bỏ khoảng trắng hoặc ký tự xuống dòng ẩn từ file properties
            String cleanToken = accessToken != null ? accessToken.trim() : "";
            
            // Encode basic auth credential (access_token + ":") với mật khẩu rỗng chuẩn xác của Basic Auth
            String credential = Base64.getEncoder().encodeToString((cleanToken + ":").getBytes(java.nio.charset.StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + credential);
            headers.set("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36");

            // Construct payload cho endpoint /sms/send
            Map<String, Object> payload = new HashMap<>();
            payload.put("to", new String[] { normalizedPhone });
            payload.put("content", messageContent);
            payload.put("type", smsType); // Đổi từ sms_type thành type cho phù hợp với endpoint mới
            
            String cleanSender = smsSender != null ? smsSender.trim() : "";
            payload.put("sender", cleanSender);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            logger.info("Sending OTP SMS to phone: {} (Normalized: {})", phoneNumber, normalizedPhone);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            logger.info("SpeedSMS API response: {}", response.getBody());
        } catch (org.springframework.web.client.HttpClientErrorException.Unauthorized e) {
            logger.warn("=== LƯU Ý: API Access Token của SpeedSMS không hợp lệ hoặc đã hết hạn (401 Unauthorized) ===");
            logger.warn("Vui lòng kiểm tra lại cấu hình 'speedsms.access.token' trong file application.properties.");
            logger.warn("Giải pháp test nhanh: Hãy lấy mã OTP [{}] đã được in ở Console Log để nhập và kiểm thử tiếp tục luồng giao diện.", otp);
        } catch (org.springframework.web.client.HttpClientErrorException.Forbidden e) {
            logger.warn("=== LƯU Ý: API SpeedSMS bị Cloudflare chặn ở môi trường Local (403 Forbidden) ===");
            logger.warn("Chi tiết: Cloudflare yêu cầu giải mã captcha/javascript khi gọi từ IP mạng cá nhân.");
            logger.warn("Giải pháp test nhanh: Hãy lấy mã OTP [{}] đã được in ở Console Log để nhập và kiểm thử tiếp tục luồng giao diện.", otp);
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
