package fdu.capstone.system.module.controller;

import fdu.capstone.system.module.service.impl.ChatBotServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: Liping Yin
 * Date: 2024/6/25
 */
@Slf4j
@RestController
@RequestMapping("/api/chatbot")
public class ChatBotController {

    @Autowired
    private ChatBotServiceImpl openAIService;

    @PostMapping("/get-response")
    public Object getResponse(HttpServletRequest request, @RequestParam Long userId, @RequestParam String sessionId) {
        String contentType = request.getContentType();
        String prompt = null;

        if (contentType != null && contentType.contains("application/json")) {
            // 处理 application/json 格式的数据
            prompt = extractJsonPrompt(request);
        } else {
            // 处理 application/x-www-form-urlencoded 格式的数据
            prompt = request.getParameter("prompt");
        }

        try {
            return openAIService.chat(userId, sessionId, prompt);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    private String extractJsonPrompt(HttpServletRequest request) {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String requestBody = stringBuilder.toString();
        return parseJsonPrompt(requestBody);
    }

    private String parseJsonPrompt(String jsonString) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, String> jsonMap = objectMapper.readValue(jsonString, HashMap.class);
            return jsonMap.get("prompt");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
