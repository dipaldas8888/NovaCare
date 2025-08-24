package com.dipal.NovaCare.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.dipal.NovaCare.service.ImageStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryImageStorageService implements ImageStorageService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder:doctors}")
    private String folder;

    public CloudinaryImageStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public UploadResult uploadDoctorImage(MultipartFile file) {
        try {
            if (file == null || file.isEmpty())
                throw new IllegalArgumentException("Empty file");

            String original = StringUtils.cleanPath(file.getOriginalFilename());
            String ext = "";
            int i = original.lastIndexOf('.');
            if (i > -1) ext = original.substring(i); // includes the dot
            String publicId = (folder == null || folder.isBlank() ? "doctors" : folder.trim())
                    + "/" + UUID.randomUUID() + ext;

            Map upload = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,        // folder + unique name
                            "overwrite", false,
                            "resource_type", "image"      // image uploads
                    )
            );

            String secureUrl = (String) upload.get("secure_url");   // https URL to store/display
            String returnedPublicId = (String) upload.get("public_id"); // store for deletes
            return new UploadResult(secureUrl, returnedPublicId);
        } catch (Exception e) {
            throw new RuntimeException("Cloudinary upload failed", e);
        }
    }

    @Override
    public void deleteByFileId(String fileId) {
        if (fileId == null || fileId.isBlank()) return;
        try {
            cloudinary.uploader().destroy(fileId, ObjectUtils.emptyMap());
        } catch (Exception ignore) {}
    }
}
