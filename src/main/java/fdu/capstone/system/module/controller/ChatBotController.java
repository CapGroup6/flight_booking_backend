package fdu.capstone.system.module.controller;

import fdu.capstone.system.module.service.impl.ChatBotServiceImpl;
import fdu.capstone.util.ResponseResult;
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
    public ResponseResult getResponse(@RequestParam Long userId, @RequestParam String sessionId, @RequestParam String prompt) {
        try {
            Object result =  openAIService.chat(userId, sessionId, prompt);
            return  ResponseResult.success(result);
        } catch (Exception e) {
            log.error("chat error: ",e);
            return ResponseResult.fail(e.getMessage());
        }
    }
}
