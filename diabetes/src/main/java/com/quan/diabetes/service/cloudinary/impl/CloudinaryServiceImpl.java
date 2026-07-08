package com.quan.diabetes.service.cloudinary.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.quan.diabetes.service.cloudinary.CloudinaryService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final List<String> ALLOWED_TYPES = List.of(
            "image/png",
            "image/jpg",
            "image/jpeg"
    );
    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 1. Kiểm tra loại file trước (Cả ContentType và File Extension)
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        boolean isValidType = false;

        if (contentType != null && ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            isValidType = true;
        }

        if (originalFilename != null && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg")) {
                isValidType = true;
            } else {
                isValidType = false; // Block extensions like .pptx even if MIME type check is bypassed
            }
        }

        if (!isValidType) {
            throw new IllegalArgumentException("Chỉ chấp nhận file JPG và PNG");
        }

        // 2. Sau đó kiểm tra kích thước
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Kích thước ảnh không được vượt quá 2MB");
        }

        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.emptyMap()
        );

        return uploadResult.get("secure_url").toString();
    }

    @Override
    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", "patient_avatars"
        ));
        return (String) uploadResult.get("secure_url");
    }
}
