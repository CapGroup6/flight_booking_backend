package fdu.capstone.system.module.controller;

import fdu.capstone.system.module.service.impl.ChatBotServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public Object getResponse(@RequestParam Long userId, @RequestParam String sessionId, @RequestParam String prompt) {
        try {
            return openAIService.chat(userId, sessionId, prompt);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
