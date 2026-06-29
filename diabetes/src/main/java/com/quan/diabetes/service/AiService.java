package com.quan.diabetes.service;

import com.quan.diabetes.dto.AiChatRequest;
import com.quan.diabetes.dto.AiChatResponse;
import com.quan.diabetes.dto.PythonAiRequest;
import com.quan.diabetes.dto.PythonAiResponse;
import com.quan.diabetes.repository.AiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AiService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AiRepository aiRepository;

    private final String PYTHON_AI_URL = "http://127.0.0.1:8000/api/ai/chat";

    public AiChatResponse processChat(AiChatRequest request) {
        // 1. Gửi request lần đầu sang Python AI
        PythonAiRequest pyRequest = new PythonAiRequest(request.getPatientId(), request.getMessage(), "");
        PythonAiResponse pyResponse = callPythonServer(pyRequest);

        // 2. Nếu AI cần dữ liệu SQL
        if (pyResponse != null && "NEED_SQL_DATA".equals(pyResponse.getStatus())) {
            Map<String, Object> tool = pyResponse.getTool();
            if (tool != null) {
                String action = (String) tool.get("action");
                String patientId = (String) tool.get("patient_id");
                
                String sqlData = fetchData(action, patientId);
                String enforcedMessage = "Bệnh nhân hỏi: \"" + request.getMessage() + "\"\n\n" +
                        "[DỮ LIỆU TỪ HỆ THỐNG]:\n" + sqlData + "\n\n" +
                        "YÊU CẦU BẮT BUỘC: Đóng vai Bác sĩ, hãy đọc dữ liệu trên và trả lời câu hỏi của bệnh nhân bằng tiếng Việt tự nhiên, thân thiện. TUYỆT ĐỐI KHÔNG in ra định dạng JSON.";

                // Trộn thẳng dữ liệu và lệnh ép buộc vào phần Message
                pyRequest.setMessage(enforcedMessage);
                pyRequest.setContextData(""); // Xóa rỗng phần này đi vì đã gộp chung ở trên
                // 3. Gửi lại kết quả SQL sang Python AI
                pyRequest.setContextData(sqlData);
                pyResponse = callPythonServer(pyRequest);
            }
        }

        // 4. Trả về kết quả cuối cùng cho Frontend
        if (pyResponse != null) {
            return new AiChatResponse("SUCCESS", pyResponse.getContent());
        }
        return new AiChatResponse("ERROR", "Không thể kết nối đến AI Server");
    }

    private String fetchData(String action, String patientId) {
        switch (action) {
            case "get_general_record":
                return aiRepository.getGeneralRecord(patientId);
            case "get_clinical_examination":
                return aiRepository.getClinicalExamination(patientId);
            case "get_treatment_plan":
                return aiRepository.getTreatmentPlan(patientId);
            case "get_lab_results":
                return aiRepository.getLabResults(patientId);
            case "get_prescriptions":
                return aiRepository.getPrescriptions(patientId);
            default:
                return "Không tìm thấy công cụ hoặc dữ liệu phù hợp.";
        }
    }

    private PythonAiResponse callPythonServer(PythonAiRequest request) {
        try {
            ResponseEntity<PythonAiResponse> response = restTemplate.postForEntity(
                    PYTHON_AI_URL, request, PythonAiResponse.class);
            return response.getBody();
        } catch (Exception e) {
            System.err.println("Lỗi khi gọi Python AI Server: " + e.getMessage());
            return null;
        }
    }
}
