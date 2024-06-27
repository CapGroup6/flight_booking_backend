package fdu.capstone.system.module.controller;
import fdu.capstone.system.module.service.impl.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
/**
 * Author: Liping Yin
 * Date: 2024/6/25
 */
@RestController
@RequestMapping("/api/chatbot")
public class OpenAIController {

    @Autowired
    private OpenAIService openAIService;

    @PostMapping("/get-response")
    public String getResponse(@RequestParam String sessionId, @RequestBody String prompt) {
        try {
            return openAIService.getCompletion(sessionId, prompt);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
