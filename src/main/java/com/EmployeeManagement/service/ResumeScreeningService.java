package com.EmployeeManagement.service;

import com.EmployeeManagement.dto.ResumeAnalysisResponse;
import com.EmployeeManagement.entity.Job;
import com.EmployeeManagement.dao.JobDAO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
            resumeText = extractTextFromUrl(resumeUrl);
        } catch (IOException e) {
            // Fallback: If URL fails (e.g. local path or relative), try to handle it
            // For now, let's assume it's a valid URL or local file path
            System.err.println("Failed to extract text from resume: " + e.getMessage());
            resumeText = "[Resume content could not be extracted]";
        }

        String prompt = String.format(
            "Analyze the following resume for the job position: %s\n\n" +
            "Job Requirements:\n%s\n\n" +
            "Resume Content:\n%s\n\n" +
            "Please provide a structured analysis including:\n" +
            "1. Match percentage (0-100)\n" +
            "2. Missing skills compared to job requirements\n" +
            "3. Key strengths of the candidate\n" +
            "4. A short summary of the candidate's profile\n" +
            "5. Extracted skills from the resume\n" +
            "6. Extracted experience (years and key roles)\n" +
            "7. Extracted education details",
            job.getTitle(),
            job.getRequiredSkills() + "\n" + job.getDescription(),
            resumeText
        );

        try {
            return geminiChatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(ResumeAnalysisResponse.class);
        } catch (Exception e) {
            // Return a fallback response so the user can still apply
            return new ResumeAnalysisResponse(0, 
                List.of("AI analysis unavailable at the moment"), 
                List.of("Please review manually"), 
                "AI screening was skipped or failed",
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
            try (InputStream in = new URL(resumeUrl).openStream();
                 PDDocument document = Loader.loadPDF(in.readAllBytes())) {
                return new PDFTextStripper().getText(document);
            }
        }
    }
}
