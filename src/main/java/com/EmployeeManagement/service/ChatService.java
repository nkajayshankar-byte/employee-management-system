package com.EmployeeManagement.service;

import com.EmployeeManagement.dao.UserDAO;
import com.EmployeeManagement.entity.User;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    
    @Autowired
    private UserDAO userDAO;
 
    public ChatService(@Qualifier("groqChatClientBuilder") ChatClient.Builder chatClientBuilder, ChatTools chatTools, ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
        this.chatClient = chatClientBuilder
                .defaultSystem("You are a professional HR AI Assistant for Phoenix.Ltd. " +
                        "Your goal is to provide concise, accurate, and relevant information. " +
                        "IMPORTANT: You only have READ-ONLY access. Do NOT try to create, update, or delete any records (like adding jobs, applying for jobs, or changing leaves). If a user asks to do these things, politely explain that you can only provide information and they should use the website forms for those actions. " +
                        "CRITICAL: Only call tools that are directly related to the user's question. Do NOT provide summaries of other unrelated data unless specifically asked. " +
                        "Always be polite and helpful. Use Markdown to format your responses beautifully. Use tables for status information and bullet points for lists. " +
                        "If you don't have enough information to answer a user-specific question (like employee ID), ask them politely or use the context provided.")
                .defaultTools(chatTools)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
 
    public String chat(String userMessage, String userEmail, String chatId) {
        final String effectiveChatId = (chatId == null || chatId.isEmpty()) ? "default" : chatId;
        String today = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now());
        String systemContext = "Current date: " + today + ". Current user email: " + (userEmail != null ? userEmail : "Guest/Applicant") + ". ";
        
        if (userEmail != null) {
            Optional<User> userOpt = userDAO.findByEmail(userEmail);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                systemContext += String.format("Logged in user details: Name: %s, ID: %d, Role: %s, Job Role: %s. ", 
                    user.getName(), user.getId(), user.getRole(), user.getJobRole());
            }
        }

        try {
            return chatClient.prompt()
                    .system(systemContext)
                    .user(userMessage)
                    .advisors(a -> a.param("chat_memory_conversation_id", effectiveChatId))
                    .call()
                    .content();
        } catch (Exception e) {
            return "I apologize, but I am currently having trouble connecting to my AI service. Please try again in a moment. Error: " + e.getMessage();
        }
    }

    public void clearHistory(String chatId) {
        if (chatId != null) {
            chatMemory.clear(chatId);
        }
    }
}
