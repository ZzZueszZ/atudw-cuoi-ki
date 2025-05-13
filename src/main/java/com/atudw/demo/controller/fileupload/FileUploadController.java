package com.atudw.demo.controller.fileupload;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/fileupload")
public class FileUploadController {

    // Directory to store uploaded files
    private final Path uploadDir;
    private final Path secureUploadDir;

    // List to store uploaded filenames and paths
    private final List<String> unsafeFiles = new ArrayList<>();
    private final List<String> safeFiles = new ArrayList<>();

    // Allowed file extensions for secure upload
    private final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");

    public FileUploadController(@Value("${file.upload-dir:./uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir + "/unsafe");
        this.secureUploadDir = Paths.get(uploadDir + "/safe");

        // Create directories if they don't exist
        try {
            Files.createDirectories(this.uploadDir);
            Files.createDirectories(this.secureUploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directories", e);
        }
    }

    @GetMapping
    public String showUploadForm(Model model) {
        model.addAttribute("unsafeFiles", unsafeFiles);
        model.addAttribute("safeFiles", safeFiles);
        return "fileupload/upload-form";
    }

    /**
     * VULNERABLE IMAGE UPLOAD - Multiple security issues:
     * 1. No proper file type validation (only checks extension)
     * 2. Uses original filename (path traversal risk)
     * 3. No content validation
     * 4. No file size limits
     * 5. Allows any file extension
     */
    @PostMapping("/unsafe")
    public String handleUnsafeUpload(@RequestParam("file") MultipartFile file,
            Model model,
            HttpServletRequest request) {
        if (file.isEmpty()) {
            model.addAttribute("errorMessage", "Please select a file to upload");
            return "fileupload/upload-form";
        }

        try {
            // UNSAFE: Using original filename directly
            String filename = StringUtils.cleanPath(file.getOriginalFilename());

            // UNSAFE: Only basic extension check but allows any extension
            if (!filename.contains(".")) {
                model.addAttribute("errorMessage", "File must have an extension");
                return "fileupload/upload-form";
            }

            // UNSAFE: No content validation or size limits

            // Save the file
            Path targetLocation = this.uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            unsafeFiles.add(filename);
            model.addAttribute("successMessage", "File uploaded successfully: " + filename);
            model.addAttribute("isVulnerable", true);
            model.addAttribute("uploadedFile", filename);

            return "fileupload/upload-result";

        } catch (IOException ex) {
            model.addAttribute("errorMessage", "Failed to upload file: " + ex.getMessage());
            return "fileupload/upload-form";
        }
    }

    /**
     * SECURE IMAGE UPLOAD - Implements proper security controls:
     * 1. Strict file type validation (extension and content)
     * 2. Generates a random filename
     * 3. Validates file is actually an image
     * 4. Implements size limits
     * 5. Re-encodes the image to remove any embedded malicious content
     */
    @PostMapping("/safe")
    public String handleSafeUpload(@RequestParam("file") MultipartFile file,
            Model model,
            HttpServletRequest request) {
        if (file.isEmpty()) {
            model.addAttribute("errorMessage", "Please select a file to upload");
            return "fileupload/upload-form";
        }

        try {
            // Get original filename for display only
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

            // SECURE: Validate file size
            if (file.getSize() > 5 * 1024 * 1024) { // 5MB limit
                model.addAttribute("errorMessage", "File size exceeds maximum limit (5MB)");
                return "fileupload/upload-form";
            }

            // SECURE: Validate file extension using whitelist
            String fileExtension = getFileExtension(originalFilename);
            if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
                model.addAttribute("errorMessage", "Only JPG, JPEG, PNG and GIF files are allowed");
                return "fileupload/upload-form";
            }

            // SECURE: Generate a random filename with the original extension
            String secureFilename = UUID.randomUUID().toString() + "." + fileExtension;

            // Save the uploaded file temporarily
            Path tempFile = Files.createTempFile("upload-", "-temp");
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            // SECURE: Validate file content is actually an image
            try {
                BufferedImage image = ImageIO.read(tempFile.toFile());
                if (image == null) {
                    Files.delete(tempFile);
                    model.addAttribute("errorMessage", "Uploaded file is not a valid image");
                    return "fileupload/upload-form";
                }

                // SECURE: Re-encode the image to remove any embedded malicious content
                Path targetLocation = this.secureUploadDir.resolve(secureFilename);
                ImageIO.write(image, fileExtension, targetLocation.toFile());

                // Delete the temp file
                Files.delete(tempFile);

                // Store the mapping between original and secure filenames
                safeFiles.add(secureFilename);

                model.addAttribute("successMessage", "File uploaded securely: " + originalFilename);
                model.addAttribute("isVulnerable", false);
                model.addAttribute("uploadedFile", secureFilename);
                model.addAttribute("originalFilename", originalFilename);

                return "fileupload/upload-result";

            } catch (IOException e) {
                Files.delete(tempFile);
                model.addAttribute("errorMessage", "Error processing image: " + e.getMessage());
                return "fileupload/upload-form";
            }

        } catch (IOException ex) {
            model.addAttribute("errorMessage", "Failed to upload file: " + ex.getMessage());
            return "fileupload/upload-form";
        }
    }

    @GetMapping("/unsafe/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveUnsafeFile(@PathVariable String filename) {
        try {
            Path filePath = this.uploadDir.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                // UNSAFE: No Content-Disposition header to force download
                // UNSAFE: No content-type validation
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, guessContentType(filename))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/safe/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveSafeFile(@PathVariable String filename) {
        try {
            Path filePath = this.secureUploadDir.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                String contentType = Files.probeContentType(filePath);

                // SECURE: Validate content type is an image type
                if (!contentType.startsWith("image/")) {
                    return ResponseEntity.badRequest().build();
                }

                // SECURE: Set proper Content-Disposition header
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Helper methods
    private String getFileExtension(String filename) {
        if (filename.lastIndexOf(".") != -1 && filename.lastIndexOf(".") != 0) {
            return filename.substring(filename.lastIndexOf(".") + 1);
        }
        return "";
    }

    private String guessContentType(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "svg":
                return "image/svg+xml";
            default:
                return "application/octet-stream";
        }
    }
}