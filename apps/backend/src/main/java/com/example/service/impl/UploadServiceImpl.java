package com.example.service.impl;

import com.example.exception.BusinessException;
import com.example.service.UploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class UploadServiceImpl implements UploadService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public String uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(400, "上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException(400, "文件名不能为空");
        }

        String ext = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            ext = originalFilename.substring(dotIndex).toLowerCase();
        }

        String allowedExt = ".jpg .jpeg .png .gif .webp";
        if (!allowedExt.contains(ext)) {
            throw new BusinessException(400, "不支持的图片格式，仅支持 jpg/png/gif/webp");
        }

        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String newFilename = UUID.randomUUID().toString().replace("-", "") + ext;

        File dir = new File(uploadDir);
        if (!dir.isAbsolute()) {
            dir = new File(System.getProperty("user.dir"), uploadDir);
        }
        File targetDir = new File(dir, dateDir);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        Path targetPath = Paths.get(targetDir.getAbsolutePath(), newFilename);

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException(500, "文件上传失败");
        }

        return "/uploads/" + dateDir + "/" + newFilename;
    }
}
