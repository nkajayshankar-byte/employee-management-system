package com.EmployeeManagement.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class FileStorageService {

    @Autowired
    private CloudinaryService cloudinaryService;

    private final Path root = Paths.get("uploads");

    public FileStorageService() {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    public String save(MultipartFile file) {
        // Use Cloudinary if configured (typically in Production/Render)
        String cloudName = System.getenv("CLOUDINARY_CLOUD_NAME");
        if (cloudName != null && !cloudName.isEmpty()) {
            try {
                return cloudinaryService.upload(file);
            } catch (IOException e) {
                throw new RuntimeException("Cloudinary upload failed: " + e.getMessage());
            }
        }

        try {
            byte[] content = file.getBytes();
            String fullHash = calculateHash(content);
            String shortHash = fullHash.substring(0, 12); // Use 12 chars for readability
            
            // Check if a file with this hash already exists
            try (Stream<Path> files = Files.list(this.root)) {
                Optional<Path> existingFile = files
                    .filter(p -> p.getFileName().toString().startsWith(shortHash + "_"))
                    .findFirst();
                
                if (existingFile.isPresent()) {
                    return "/uploads/" + existingFile.get().getFileName().toString();
                }
            }

            // Clean up original filename (remove existing hash prefixes if any)
            String originalName = file.getOriginalFilename();
            if (originalName != null && originalName.matches("^[a-f0-9]{12,64}_.*")) {
                originalName = originalName.substring(originalName.indexOf("_") + 1);
            }

            String filename = shortHash + "_" + originalName;
            Files.write(this.root.resolve(filename), content);
            return "/uploads/" + filename;
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    private String calculateHash(byte[] content) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] hash = digest.digest(content);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
