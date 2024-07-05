package fdu.capstone.system.module.service.impl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
/**
 * Author: Liping Yin
 * Date: 2024/6/25
 */
@Service
public class ChatBotService {

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    private final RestTemplate restTemplate = new RestTemplate();

    // Map to store conversation history for each user session
    private final Map<String, List<Map<String, String>>> conversationHistory = new HashMap<>();

    public synchronized String getCompletion(String sessionId, String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        List<Map<String, String>> messages = conversationHistory.computeIfAbsent(sessionId, k -> new ArrayList<>());

        // Add the new user message to the conversation history
        messages.add(new HashMap<String, String>() {{
            put("role", "user");
            put("content", prompt);
        }});

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4");
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                OPENAI_API_URL,
                HttpMethod.POST,
                entity,
                String.class
        );

        String responseBody = response.getBody();

        // Add the assistant's response to the conversation history
        messages.add(new HashMap<String, String>() {{
            put("role", "assistant");
            put("content", parseContentFromResponse(responseBody));
        }});

        return responseBody;
    }

    private String parseContentFromResponse(String response) {
        // Parse the response body to extract the assistant's message content
        // This is a simplified example, you might need a proper JSON parsing logic here
        int startIndex = response.indexOf("\"content\":\"") + 11;
        int endIndex = response.indexOf("\"", startIndex);
        return response.substring(startIndex, endIndex);
    }
}

