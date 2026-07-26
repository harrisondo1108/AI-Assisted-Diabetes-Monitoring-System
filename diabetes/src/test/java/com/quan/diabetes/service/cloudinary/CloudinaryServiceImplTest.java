package com.quan.diabetes.service.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import com.quan.diabetes.service.cloudinary.impl.CloudinaryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceImplTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryServiceImpl cloudinaryService;

    @BeforeEach
    void setUp() {
        // leniency for tests that don't call uploader
        lenient().when(cloudinary.uploader()).thenReturn(uploader);
    }

    @Test
    @DisplayName("uploadFile - Null file should return null")
    void uploadFile_NullFile_ReturnsNull() throws IOException {
        String result = cloudinaryService.uploadFile(null);
        assertNull(result);
    }

    @Test
    @DisplayName("uploadFile - Empty file should return null")
    void uploadFile_EmptyFile_ReturnsNull() throws IOException {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "image/png", new byte[0]);
        String result = cloudinaryService.uploadFile(emptyFile);
        assertNull(result);
    }

    @Test
    @DisplayName("uploadFile - Valid PNG file within 2MB should succeed")
    void uploadFile_ValidPng_Success() throws IOException {
        byte[] content = "fake image content".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", content);
        Map<String, Object> uploadResult = Map.of("secure_url", "https://cloudinary.com/test.png");

        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        String url = cloudinaryService.uploadFile(file);
        assertEquals("https://cloudinary.com/test.png", url);
    }

    @Test
    @DisplayName("uploadFile - Valid JPG file within 2MB should succeed")
    void uploadFile_ValidJpg_Success() throws IOException {
        byte[] content = "fake image content".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpeg", "image/jpeg", content);
        Map<String, Object> uploadResult = Map.of("secure_url", "https://cloudinary.com/photo.jpeg");

        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        String url = cloudinaryService.uploadFile(file);
        assertEquals("https://cloudinary.com/photo.jpeg", url);
    }

    @Test
    @DisplayName("uploadFile - Invalid ContentType and extension should throw IllegalArgumentException")
    void uploadFile_InvalidType_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf", new byte[100]);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            cloudinaryService.uploadFile(file);
        });
        assertEquals("Chỉ chấp nhận file JPG và PNG", exception.getMessage());
    }

    @Test
    @DisplayName("uploadFile - Invalid extension even if ContentType is valid should throw IllegalArgumentException")
    void uploadFile_InvalidExtension_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile("file", "malicious.exe", "image/png", new byte[100]);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            cloudinaryService.uploadFile(file);
        });
        assertEquals("Chỉ chấp nhận file JPG và PNG", exception.getMessage());
    }

    @Test
    @DisplayName("uploadFile - File size exceeding 2MB should throw IllegalArgumentException")
    void uploadFile_ExceedsSizeLimit_ThrowsException() {
        byte[] largeContent = new byte[2 * 1024 * 1024 + 1]; // 2MB + 1 byte
        MockMultipartFile file = new MockMultipartFile("file", "large.png", "image/png", largeContent);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            cloudinaryService.uploadFile(file);
        });
        assertEquals("Kích thước ảnh không được vượt quá 2MB", exception.getMessage());
    }

    @Test
    @DisplayName("uploadImage - Null file should return null")
    void uploadImage_NullFile_ReturnsNull() throws IOException {
        String result = cloudinaryService.uploadImage(null);
        assertNull(result);
    }

    @Test
    @DisplayName("uploadImage - Empty file should return null")
    void uploadImage_EmptyFile_ReturnsNull() throws IOException {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "image/png", new byte[0]);
        String result = cloudinaryService.uploadImage(emptyFile);
        assertNull(result);
    }

    @Test
    @DisplayName("uploadImage - Valid avatar file should upload to patient_avatars folder")
    void uploadImage_ValidFile_Success() throws IOException {
        byte[] content = "avatar content".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", content);
        Map<String, Object> uploadResult = Map.of("secure_url", "https://cloudinary.com/patient_avatars/avatar.jpg");

        when(uploader.upload(any(byte[].class), eq(ObjectUtils.asMap("folder", "patient_avatars")))).thenReturn(uploadResult);

        String url = cloudinaryService.uploadImage(file);
        assertEquals("https://cloudinary.com/patient_avatars/avatar.jpg", url);
    }
}
