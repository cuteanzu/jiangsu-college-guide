package com.jiangsu.guide.controller;

import com.jiangsu.guide.common.Result;
import com.jiangsu.guide.common.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api")
public class FileController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.url-prefix:/uploads}")
    private String urlPrefix;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (file.isEmpty()) {
            return Result.fail(400, "请选择要上传的文件");
        }

        // 限制文件大小
        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.fail(400, "文件大小不能超过 10MB");
        }

        // 限制文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.fail(400, "只允许上传图片文件");
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String filename = UUID.randomUUID() + ext;

            Path targetPath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String url = urlPrefix + "/" + filename;
            log.info("✔ 文件上传成功: userId={}, file={}, url={}", userId, filename, url);
            return Result.ok(url);
        } catch (IOException e) {
            log.error("✘ 文件上传失败: {}", e.getMessage());
            return Result.fail(500, "文件上传失败，请稍后重试");
        }
    }
}
