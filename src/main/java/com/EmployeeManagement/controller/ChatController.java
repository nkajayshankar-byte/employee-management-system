package com.EmployeeManagement.controller;

import com.EmployeeManagement.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping
    public Map<String, String> chat(@RequestBody Map<String, String> payload, Principal principal) {
        String message = payload.get("message");
        String chatId = payload.get("chatId");
        String email = (principal != null) ? principal.getName() : null;
        
        String response = chatService.chat(message, email, chatId);
        return Map.of("response", response);
    }

    @DeleteMapping("/history/{chatId}")
    public Map<String, String> clearHistory(@PathVariable String chatId) {
        chatService.clearHistory(chatId);
        return Map.of("status", "History cleared for " + chatId);
    }
}
