package com.quan.diabetes.service.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Upload an image to Cloudinary.
     *
     * @param file the multipart file to upload
     * @return the secure URL of the uploaded image, or null if the file is empty/invalid
     * @throws IOException if the upload fails
     */
    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        // Upload bytes directly using the SDK uploader
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", "patient_avatars"
        ));
        
        return (String) uploadResult.get("secure_url");
    }
}
