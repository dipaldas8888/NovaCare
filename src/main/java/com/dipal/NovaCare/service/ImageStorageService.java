package com.dipal.NovaCare.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {
    class UploadResult {
        public final String url;
        public final String fileId;
        public UploadResult(String url, String fileId) { this.url = url; this.fileId = fileId; }
    }
    UploadResult uploadDoctorImage(MultipartFile file);
    void deleteByFileId(String fileId);
}
