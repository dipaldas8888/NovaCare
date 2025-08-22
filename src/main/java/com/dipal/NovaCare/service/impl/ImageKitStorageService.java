package com.dipal.NovaCare.service.impl;

// src/main/java/com/dipal/NovaCare/service/storage/ImageKitStorageService.java

import com.dipal.NovaCare.service.ImageStorageService;
import io.imagekit.sdk.ImageKit;
import io.imagekit.sdk.models.FileCreateRequest;
import io.imagekit.sdk.models.results.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@Service
public class ImageKitStorageService implements ImageStorageService {

    private final ImageKit imageKit;

    @Value("${imagekit.folder:/doctors}")
    private String folder;

    public ImageKitStorageService(ImageKit imageKit) {
        this.imageKit = imageKit;
    }

    @Override
    public UploadResult uploadDoctorImage(MultipartFile file) {
        try {
            String original = StringUtils.cleanPath(file.getOriginalFilename());
            String b64 = Base64.getEncoder().encodeToString(file.getBytes());
            FileCreateRequest req = new FileCreateRequest(b64, original);
            req.setFolder(folder);
            // req.setUseUniqueFileName(true); // optional

            Result res = imageKit.upload(req);     // returns url + fileId
            return new UploadResult(res.getUrl(), res.getFileId());
        } catch (Exception e) {
            throw new RuntimeException("ImageKit upload failed", e);
        }
    }

    @Override
    public void deleteByFileId(String fileId) {
        if (fileId == null || fileId.isBlank()) return;
        try { imageKit.deleteFile(fileId); } catch (Exception ignore) {}
    }
}
