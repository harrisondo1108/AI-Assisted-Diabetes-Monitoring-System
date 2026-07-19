package com.quan.diabetes.service.cloudinary;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface CloudinaryService {
    String uploadFile(MultipartFile file) throws IOException;
    String uploadImage(MultipartFile file) throws IOException;
}
