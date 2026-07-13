package com.EmployeeManagement.service;

import com.EmployeeManagement.dto.ResumeAnalysisResponse;
import com.EmployeeManagement.entity.Job;
import com.EmployeeManagement.dao.JobDAO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
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
        	    "You are a highly objective HR AI analysis system. Analyze the following resume against the job requirements.\n\n" +
        	    "Job Position: %s\n\n" +
        	    "Job Requirements:\n%s\n\n" +
        	    "Resume Content:\n%s\n\n" +
        	    "CRITICAL SKILL MATCHING RULES - Follow these STRICTLY:\n" +
        	    "Skill matching is BIDIRECTIONAL. A general skill satisfies a specific requirement AND vice versa:\n" +
        	    "- SQL, MySQL, PostgreSQL, MariaDB, Oracle DB, SQL Server are ALL equivalent. If the resume has ANY of these, it satisfies a requirement for ANY of the others.\n" +
        	    "- MongoDB, NoSQL, DynamoDB, Cassandra are ALL equivalent for NoSQL requirements.\n" +
        	    "- Java, Spring, Spring Boot are ALL equivalent. Having ANY one satisfies a requirement for ANY of the others.\n" +
        	    "- JavaScript, TypeScript, React, Angular, Vue.js, Node.js are ALL in the same family. Having ANY one satisfies a JavaScript/Frontend requirement.\n" +
        	    "- Python, Django, Flask, FastAPI are ALL in the same family.\n" +
        	    "- AWS, Azure, GCP, Cloud are ALL equivalent for cloud requirements.\n" +
        	    "- REST APIs, REST, RESTful, HTTP APIs, Microservices are ALL equivalent.\n" +
        	    "- Docker, Kubernetes, Containerization are ALL equivalent.\n" +
        	    "- Git, GitHub, GitLab, Bitbucket are ALL equivalent for version control.\n" +
        	    "- Do NOT list a skill as 'missing' if the candidate has ANY related, equivalent, superset, or subset technology from the same family.\n" +
        	    "- Only list a skill as truly missing if the candidate has ZERO related expertise.\n\n" +
        	    "CRITICAL SCORING RULES (MUST BE STRICTLY DETERMINISTIC):\n" +
        	    "To prevent score fluctuations, calculate the Match Percentage (0-100) exactly as follows:\n" +
        	    "1. Count the exact number of Technical Skills required by the job (Let this be T).\n" +
        	    "2. Count how many of those skills (or their bidirectional equivalents) are present in the resume (Let this be M).\n" +
        	    "3. The final Match Percentage MUST BE exactly (M / T) * 100, rounded to the nearest integer.\n" +
        	    "4. Do NOT adjust the score subjectively based on experience, education, or formatting. The score MUST be a pure mathematical calculation of skill overlap so it is identical every time.\n\n" +
        	    "Analyze the candidate and provide a structured response:\n" +
        	    "1. matchPercentage (int) - The strictly calculated (M / T) * 100 score.\n" +
        	    "2. missingSkills (array of strings) - ONLY required skills with ZERO related match.\n" +
        	    "3. strengths (array of strings) - The candidate's best matching skills and experience.\n" +
        	    "4. summary (string) - A short summary explaining the skills found vs missing.\n" +
        	    "5. extractedSkills: Extracted technical skills from resume\n" +
        	    "6. extractedExperience: Total experience in years\n" +
        	    "7. extractedEducation: Highest education details\n" +
        	    "8. extractedName: The candidate's full name\n" +
        	    "9. extractedEmail: The candidate's email address\n" +
        	    "10. extractedPhone: The candidate's phone number\n" +
        	    "11. extractedLinkedIn: The candidate's LinkedIn URL (if any)\n" +
        	    "12. extractedGitHub: The candidate's GitHub URL (if any)\n\n" +
        	    "CRITICAL: Respond ONLY with a valid JSON object. No markdown, no code fences, no explanation.\n" +
        	    "The JSON must have these exact keys: matchPercentage (int), missingSkills (array of strings), strengths (array of strings), summary (string), extractedSkills (string), extractedExperience (string), extractedEducation (string), extractedName (string), extractedEmail (string), extractedPhone (string), extractedLinkedIn (string), extractedGitHub (string).",
        	    job.getTitle(),
        	    job.getRequiredSkills() + "\n" + job.getDescription(),
        	    resumeText
        	);

        try {
            System.out.println("Calling Groq (Llama 70B) for resume screening analysis...");
            String rawJson = aiChatClient.prompt()
                    .options(OpenAiChatOptions.builder().temperature(0.0).build())
                    .user(prompt)
                    .call()
                    .content();
            
            if (rawJson == null || rawJson.isBlank()) {
                throw new RuntimeException("AI returned a null/empty response");
            }
            
            // Strip markdown code fences if the LLM wraps it
            rawJson = rawJson.trim();
            if (rawJson.startsWith("```")) {
                rawJson = rawJson.replaceAll("^```(?:json)?\\s*", "").replaceAll("\\s*```$", "");
            }
            
            ObjectMapper mapper = new ObjectMapper();
            ResumeAnalysisResponse response = mapper.readValue(rawJson, ResumeAnalysisResponse.class);
            
            System.out.println("AI analysis successful. Match Score: " + response.getMatchPercentage() + "%");
            return response;
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR during AI screening: " + e.getMessage());
            System.err.println("Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "Unknown"));
            e.printStackTrace();
            
            // Provide specific hints based on common errors
            String errorDetail = e.getMessage();
            if (resumeText == null || resumeText.startsWith("[Resume content could not be extracted")) {
                errorDetail = "Resume extraction failed. Check Cloudinary URL access and PDF integrity.";
            } else if (errorDetail != null && errorDetail.contains("401")) {
                errorDetail = "AI Service authentication failed (401). Please check GROQ_API_KEY.";
            } else if (errorDetail != null && errorDetail.contains("429")) {
                errorDetail = "Rate limit exceeded (429). Please try again later.";
            }
            
            // Return a fallback response so the application remains stable
            ResumeAnalysisResponse fallback = new ResumeAnalysisResponse();
            fallback.setMatchPercentage(0);
            fallback.setMissingSkills(List.of("AI analysis unavailable: " + errorDetail));
            fallback.setStrengths(List.of("Manual review required"));
            fallback.setSummary("AI screening failed. Summary: " + errorDetail);
            fallback.setExtractedSkills("Failed to extract");
            fallback.setExtractedExperience("Failed to extract");
            fallback.setExtractedEducation("Failed to extract");
            fallback.setExtractedName("");
            fallback.setExtractedEmail("");
            fallback.setExtractedPhone("");
            fallback.setExtractedLinkedIn("");
            fallback.setExtractedGitHub("");
            return fallback;
        }
    }

    private String extractTextFromUrl(String resumeUrl) throws IOException {
        // Handle relative URLs (like /uploads/...)
        if (resumeUrl.startsWith("/")) {
            String filePath = resumeUrl.substring(1); // remove leading /
            File file = new File(filePath);
            
            if (filePath.toLowerCase().endsWith(".doc") || filePath.toLowerCase().endsWith(".docx")) {
                try (FileInputStream fis = new FileInputStream(file);
                     XWPFDocument doc = new XWPFDocument(fis);
						XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
                    return extractor.getText();
                }
            } else {
                try (PDDocument document = Loader.loadPDF(file)) {
                    return new PDFTextStripper().getText(document);
                }
            }
        } else {
            // Remote URL (Cloudinary)
            System.out.println("Opening connection to remote URL: " + resumeUrl);
            // Encode special chars in URL (e.g. brackets from duplicate filenames like [1])
            String encodedUrl = resumeUrl.replace("[", "%5B").replace("]", "%5D").replace(" ", "%20");
            URL url = URI.create(encodedUrl).toURL(); // URL url = new URL(encodedUrl);-- Deprecated
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            // Add comprehensive browser headers to avoid being blocked by CDNs (like Cloudinary/Render)
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            conn.setRequestProperty("Accept", "application/pdf,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            conn.setConnectTimeout(15000); // 15 seconds
            conn.setReadTimeout(20000);    // 20 seconds
            
            int responseCode = conn.getResponseCode();
            System.out.println("HTTP Response Code from resume download: " + responseCode);
            
            if (responseCode != 200) {
                throw new IOException("Failed to download resume. HTTP Response Code: " + responseCode);
            }

            try (InputStream in = conn.getInputStream()) {
                byte[] bytes = in.readAllBytes();
                System.out.println("Read " + bytes.length + " bytes from remote URL.");
                
                if (resumeUrl.toLowerCase().contains(".doc") || resumeUrl.toLowerCase().contains(".docx")) {
                    try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(bytes));
							XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
                        return extractor.getText();
                    }
                } else {
                    try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(bytes))) {
                        return new PDFTextStripper().getText(document);
                    }
                }
            }
        }
    }
}
