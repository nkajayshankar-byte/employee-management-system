package com.EmployeeManagement.service;

import com.EmployeeManagement.dto.ResumeAnalysisResponse;
import com.EmployeeManagement.entity.Job;
import com.EmployeeManagement.dao.JobDAO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Service
public class ResumeScreeningService {

    private final ChatClient geminiChatClient;

    @Autowired
    private JobDAO jobDAO;

    public ResumeScreeningService(GoogleGenAiChatModel chatModel) {
        this.geminiChatClient = ChatClient.builder(chatModel).build();
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
            System.out.println("Calling Gemini for resume screening analysis...");
            ResumeAnalysisResponse response = geminiChatClient.prompt()
                    .options(GoogleGenAiChatOptions.builder().temperature(0.0).build())
                    .user(prompt)
                    .call()
                    .entity(ResumeAnalysisResponse.class);
            System.out.println("AI analysis successful.");
            return response;
        } catch (Exception e) {
            System.err.println("ERROR during AI screening: " + e.getMessage());
            e.printStackTrace();
            // Return a fallback response so the user can still apply
            return new ResumeAnalysisResponse(0, 
                List.of("AI analysis unavailable at the moment"), 
                List.of("Please review manually"), 
                "AI screening was skipped or failed due to: " + e.getMessage(),
                "N/A", "N/A", "N/A");
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
            conn.setConnectTimeout(10000); // 10 seconds
            conn.setReadTimeout(15000);    // 15 seconds
            
            int responseCode = conn.getResponseCode();
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
