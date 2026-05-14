package com.EmployeeManagement.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient.Builder groqChatClientBuilder(org.springframework.ai.openai.OpenAiChatModel groqChatModel) {
        return ChatClient.builder(groqChatModel);
    }

    @Bean
    public ChatClient.Builder geminiChatClientBuilder(org.springframework.ai.google.genai.GoogleGenAiChatModel geminiChatModel) {
        return ChatClient.builder(geminiChatModel);
    }
}
