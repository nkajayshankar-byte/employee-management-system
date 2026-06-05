package com.EmployeeManagement.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Url;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

        byte[] fileBytes = file.getBytes();
        String fullHash = calculateHash(fileBytes);
        String shortHash = fullHash.substring(0, 12);

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
            
            // For 'raw' resource type, we must include the extension in the public_id
            if ("raw".equals(resourceType)) {
                publicId = baseName + "_" + shortHash + extension;
            } else {
                publicId = baseName + "_" + shortHash;
            }
        } else {
            publicId = "file_" + shortHash;
        }

        // Construct expected delivery URL
        Url urlBuilder = cloudinary.url().resourceType(resourceType).secure(true);
        if ("image".equals(resourceType) && originalFilename != null && originalFilename.lastIndexOf('.') > 0) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
            urlBuilder = urlBuilder.format(ext);
        }
        String urlString = urlBuilder.generate("resumes/" + publicId);

        // Check if the file already exists on Cloudinary
        if (checkIfFileExists(urlString)) {
            return urlString;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader()
                .upload(fileBytes, ObjectUtils.asMap(
                        "resource_type", resourceType,
                        "public_id", "resumes/" + publicId,
                        "access_mode", "public"
                ));

        return uploadResult.get("secure_url").toString();
    }

    private String calculateHash(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(content);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    private boolean checkIfFileExists(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            // Fallback to upload if check fails
            return false;
        }
    }
}
