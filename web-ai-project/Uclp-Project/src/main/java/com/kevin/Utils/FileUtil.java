package com.kevin.Utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
public class FileUtil {
    // 1.统一上传目录
    private static final String UPLOAD_DIR = "D:/uploads/";

    // 2.允许上传的文件类型白名单
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "zip", "rar", "7z", "tar.gz"
    );

    public static String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("无法获取原始文件名");
        }

        // 文件类型安全校验
        String extension = getFileExtension(originalFilename);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("不支持的文件类型：" + extension);
        }

        // 生成唯一文件名（防止文件名冲突和中文乱码问题）
        String newFileName = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + "." + extension;

        // 构建保存路径
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("自动创建上传目录: {}", uploadPath.toAbsolutePath());
        }

        // 保存文件到磁盘
        Path filePath = uploadPath.resolve(newFileName);
        file.transferTo(filePath);
        log.info("文件上传成功: {} -> {}", originalFilename, filePath.toAbsolutePath());

        // 返回 Web 访问路径（注意：这里返回的路径必须与配置类中的映射一致）
        return "/uploads/" + newFileName;
    }

    // 提取文件扩展名
    private static String getFileExtension(String filename) {
        int lastIndexOfDot = filename.lastIndexOf(".");
        if (lastIndexOfDot == -1) {
            return "";
        }
        return filename.substring(lastIndexOfDot + 1);
    }
}