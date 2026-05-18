package com.EmployeeManagement.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud_name}") String cloudName,
            @Value("${cloudinary.api_key}") String apiKey,
            @Value("${cloudinary.api_secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    public String upload(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        
        // Cloudinary handles PDFs and Images as 'image' resource_type
        // Word documents and other files should be 'raw'
        boolean isViewable = contentType != null && (contentType.startsWith("image/") || contentType.equals("application/pdf"));
        String resourceType = isViewable ? "image" : "raw";

        String publicId;
        if (originalFilename != null && !originalFilename.isEmpty()) {
            int lastDot = originalFilename.lastIndexOf('.');
            String baseName = lastDot > 0 ? originalFilename.substring(0, lastDot) : originalFilename;
            String extension = lastDot > 0 ? originalFilename.substring(lastDot) : "";
            
            // Sanitize baseName: only keep alphanumeric, hyphens, and underscores
            baseName = baseName.replaceAll("[^a-zA-Z0-9-_]", "_");
            if (baseName.isEmpty()) {
                baseName = "file";
            }
            
            String uniqueSuffix = "_" + java.util.UUID.randomUUID().toString().substring(0, 8);
            
            // For 'raw' resource type, we must include the extension in the public_id
            if ("raw".equals(resourceType)) {
                publicId = baseName + uniqueSuffix + extension;
            } else {
                publicId = baseName + uniqueSuffix;
            }
        } else {
            publicId = "file_" + java.util.UUID.randomUUID().toString().substring(0, 8);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader()
                .upload(file.getBytes(), ObjectUtils.asMap(
                        "resource_type", resourceType,
                        "public_id", "resumes/" + publicId
                ));

        return uploadResult.get("secure_url").toString();
    }
}
