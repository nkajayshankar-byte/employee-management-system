package com.EmployeeManagement.service;

import com.EmployeeManagement.dto.ResumeAnalysisResponse;
import com.EmployeeManagement.entity.Job;
import com.EmployeeManagement.dao.JobDAO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Service
public class ResumeScreeningService {

    private final ChatClient aiChatClient;

    @Autowired
    private JobDAO jobDAO;

    public ResumeScreeningService(@Qualifier("groqChatClientBuilder") ChatClient.Builder chatClientBuilder) {
        this.aiChatClient = chatClientBuilder.build();
    }

    public ResumeAnalysisResponse screenResume(String resumeUrl, Long jobId) {
        Optional<Job> jobOpt = jobDAO.findById(jobId);
        if (jobOpt.isEmpty()) {
            throw new RuntimeException("Job not found for screening");
        }
        Job job = jobOpt.get();

        String resumeText = "";
        try {
            System.out.println("Attempting to extract text from resume URL: " + resumeUrl);
            resumeText = extractTextFromUrl(resumeUrl);
            if (resumeText == null || resumeText.trim().isEmpty()) {
                System.err.println("Warning: Extracted resume text is empty");
                resumeText = "[Extracted resume text is empty]";
            } else {
                System.out.println("Successfully extracted " + resumeText.length() + " characters from resume.");
            }
        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to extract text from resume: " + e.getMessage());
            e.printStackTrace();
            resumeText = "[Resume content could not be extracted: " + e.getMessage() + "]";
        }

        String prompt = String.format(
            "You are a highly objective and systematic HR AI analysis system. Your task is to analyze the following resume against the job requirements with absolute consistency.\n\n" +
            "Job Position: %s\n\n" +
            "Job Requirements:\n%s\n\n" +
            "Resume Content:\n%s\n\n" +
            "Analyze the candidate strictly and provide a structured response in the required format. Ensure the Match Percentage is calculated based on objective skill alignment, experience relevance, and education matches.\n\n" +
            "Please provide:\n" +
            "1. Match percentage (0-100)\n" +
            "2. Missing skills (as a list of strings)\n" +
            "3. Key strengths (as a list of strings)\n" +
            "4. A short summary\n" +
            "5. Extracted skills\n" +
            "6. Extracted experience\n" +
            "7. Extracted education details",
            job.getTitle(),
            job.getRequiredSkills() + "\n" + job.getDescription(),
            resumeText
        );

        try {
            System.out.println("Calling Groq (Llama 70B) for resume screening analysis...");
            ResumeAnalysisResponse response = aiChatClient.prompt()
                    .options(OpenAiChatOptions.builder().temperature(0.0).build())
                    .user(prompt)
                    .call()
                    .entity(ResumeAnalysisResponse.class);
            
            if (response == null) {
                throw new RuntimeException("Gemini returned a null response");
            }
            
            System.out.println("AI analysis successful. Match Score: " + response.getMatchPercentage() + "%");
            return response;
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR during AI screening: " + e.getMessage());
            System.err.println("Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "Unknown"));
            e.printStackTrace();
            
            // Provide specific hints based on common errors
            String errorDetail = e.getMessage();
            if (errorDetail != null && errorDetail.contains("401")) {
                errorDetail = "API Key authentication failed (401). Please check GOOGLE_AI_GEMINI_API_KEY.";
            } else if (errorDetail != null && errorDetail.contains("429")) {
                errorDetail = "Rate limit exceeded (429). Please try again later.";
            }
            
            // Return a fallback response so the application remains stable
            return new ResumeAnalysisResponse(0, 
                List.of("AI analysis unavailable: " + errorDetail), 
                List.of("Manual review required"), 
                "AI screening failed. Summary: " + errorDetail,
                "Failed to extract", "Failed to extract", "Failed to extract");
        }
    }

    private String extractTextFromUrl(String resumeUrl) throws IOException {
        // Handle relative URLs (like /uploads/...)
        if (resumeUrl.startsWith("/")) {
            // Assuming this is running on a server where /uploads is in the root or accessible
            // For local development, we might need the full path
            // Let's use a simple check
            String filePath = resumeUrl.substring(1); // remove leading /
            try (PDDocument document = Loader.loadPDF(new File(filePath))) {
                return new PDFTextStripper().getText(document);
            }
        } else {
            // Remote URL (Cloudinary)
            System.out.println("Opening connection to remote URL: " + resumeUrl);
            java.net.URL url = new java.net.URL(resumeUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            
            // Add User-Agent to avoid 401/403 errors from CDNs that block default Java agents
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            conn.setConnectTimeout(10000); // 10 seconds
            conn.setReadTimeout(15000);    // 15 seconds
            
            int responseCode = conn.getResponseCode();
            System.out.println("HTTP Response Code from resume download: " + responseCode);
            
            if (responseCode != 200) {
                throw new IOException("Failed to download resume. HTTP Response Code: " + responseCode);
            }

            try (InputStream in = conn.getInputStream()) {
                byte[] bytes = in.readAllBytes();
                System.out.println("Read " + bytes.length + " bytes from remote URL.");
                try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(bytes))) {
                    return new PDFTextStripper().getText(document);
                }
            }
        }
    }
}
