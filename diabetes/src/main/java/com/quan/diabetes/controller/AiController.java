package com.quan.diabetes.controller;

import com.quan.diabetes.dto.AiChatRequest;
import com.quan.diabetes.dto.AiChatResponse;
import com.quan.diabetes.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*") // Cho phép frontend gọi API
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping
    public ResponseEntity<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        if (request.getPatientId() == null || request.getMessage() == null) {
            return ResponseEntity.badRequest().body(new AiChatResponse("ERROR", "patientId và message không được để trống"));
        }
        
        AiChatResponse response = aiService.processChat(request);
        return ResponseEntity.ok(response);
    }
}
