package com.atudw.demo.controller.pathtraversal;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/pathtraversal")
public class PathTraversalController {

    // Đường dẫn cơ sở đến thư mục uploads
    private final Path uploadsBaseDir;

    public PathTraversalController(@Value("${file.upload-dir:./uploads}") String uploadsDir) {
        this.uploadsBaseDir = Paths.get(uploadsDir);
    }

    @GetMapping
    public String showIndexPage(Model model, HttpServletRequest request) {
        try {
            // Lấy danh sách các file trong thư mục unsafe (công khai)
            List<String> publicFiles = new ArrayList<>();
            try (Stream<Path> paths = Files.list(uploadsBaseDir.resolve("unsafe"))) {
                publicFiles = paths
                        .filter(Files::isRegularFile)
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .collect(Collectors.toList());
            }

            model.addAttribute("publicFiles", publicFiles);

        } catch (IOException e) {
            model.addAttribute("error", "Không thể đọc danh sách file: " + e.getMessage());
        }

        return "pathtraversal/index";
    }

    /**
     * Endpoint có lỗ hổng Path Traversal - KHÔNG an toàn
     * Cho phép đọc file từ bất kỳ đâu thông qua ký tự ../
     */
    @GetMapping("/unsafe/view")
    public String viewFileUnsafe(@RequestParam("file") String fileName, Model model) {
        try {
            // LỖ HỔNG: Không kiểm soát đường dẫn, cho phép path traversal
            // Kẻ tấn công có thể dùng "../secret/secret.txt" làm tham số file

            // Đường dẫn đến file được yêu cầu (từ thư mục unsafe)
            Path filePath = uploadsBaseDir.resolve("unsafe").resolve(fileName);

            // Đọc và hiển thị nội dung file
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }

            model.addAttribute("fileName", fileName);
            model.addAttribute("fileContent", content.toString());
            model.addAttribute("isVulnerable", true);

        } catch (IOException e) {
            model.addAttribute("error", "Không thể đọc file: " + e.getMessage());
        }

        return "pathtraversal/view";
    }

    /**
     * Endpoint an toàn - CÓ kiểm soát đường dẫn
     */
    @GetMapping("/safe/view")
    public String viewFileSafe(@RequestParam("file") String fileName, Model model) {
        try {
            // Kiểm tra tên file có hợp lệ không - ngăn chặn path traversal
            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                model.addAttribute("error", "Tên file không hợp lệ!");
                return "pathtraversal/view";
            }
            Path filePath = uploadsBaseDir.resolve("unsafe").resolve(fileName);

            // Đọc và hiển thị nội dung file
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }

            model.addAttribute("fileName", fileName);
            model.addAttribute("fileContent", content.toString());
            model.addAttribute("isVulnerable", false);

        } catch (IOException e) {
            model.addAttribute("error", "Không thể đọc file: " + e.getMessage());
        }

        return "pathtraversal/view";
    }

    /**
     * Endpoint để download file - cũng có lỗ hổng Path Traversal
     */
    @GetMapping("/unsafe/download")
    @ResponseBody
    public ResponseEntity<Resource> downloadFileUnsafe(@RequestParam("file") String fileName) {
        try {
            // LỖ HỔNG: Không kiểm soát đường dẫn, cho phép path traversal
            Path filePath = uploadsBaseDir.resolve("unsafe").resolve(fileName);
            Resource resource = new UrlResource(filePath.toUri());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}