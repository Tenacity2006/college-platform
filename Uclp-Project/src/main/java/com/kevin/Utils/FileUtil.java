package com.kevin.Utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
public class FileUtil {
    // 统一上传目录（相对路径）
    private static final String UPLOAD_DIR = "D:/uploads";
    public static String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 获取原始文件名和后缀
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("无法获取文件名");
        }

        // 提取后缀名
        int dotIndex = originalFilename.lastIndexOf(".");
        String extension = (dotIndex != -1) ? originalFilename.substring(dotIndex) : "";
        // 安全清洗
        extension = extension.replaceAll("[^a-zA-Z0-9.]", "");
        if (extension.isEmpty()) {
            extension = ".bin"; // 默认后缀
        }

        // 生成唯一文件名
        String newFileName = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + extension;

        // 构建保存路径
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath); // 自动创建文件夹
            log.info("自动创建上传目录: {}", uploadPath.toAbsolutePath());
        }

        // 保存文件
        Path filePath = uploadPath.resolve(newFileName);
        file.transferTo(filePath);
        log.info("文件上传成功: {} -> {}", originalFilename, filePath.toAbsolutePath());

        // 返回 Web访问路径
        return "/uploads/" + newFileName;
    }
}

