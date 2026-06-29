//package org.example.sumoney.services;
//
//import io.minio.BucketExistsArgs;
//import io.minio.GetObjectArgs;
//import io.minio.MakeBucketArgs;
//import io.minio.MinioClient;
//import io.minio.PutObjectArgs;
//import io.minio.RemoveObjectArgs;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.InputStream;
//import java.util.Locale;
//import java.util.Set;
//import java.util.UUID;
//
//@Service
//public class FileStorageService {
//
//    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
//            "image/jpeg",
//            "image/png",
//            "image/webp",
//            "application/pdf"
//    );
//
//    private final MinioClient minioClient;
//    private final String bucketName;
//
//    public FileStorageService(
//            MinioClient minioClient,
//            @Value("${minio.bucket}") String bucketName
//    ) {
//        this.minioClient = minioClient;
//        this.bucketName = bucketName;
//        ensureBucketExists();
//    }
//
//    public StoredFile uploadReceipt(MultipartFile file) {
//        validateFile(file);
//
//        try {
//            String extension = extractExtension(file.getOriginalFilename());
//            String objectName = "receipts/" + UUID.randomUUID() + extension;
//
//            minioClient.putObject(
//                    PutObjectArgs.builder()
//                            .bucket(bucketName)
//                            .object(objectName)
//                            .stream(file.getInputStream(), file.getSize(), -1L)
//                            .contentType(file.getContentType())
//                            .build()
//            );
//
//            return new StoredFile(
//                    objectName,
//                    file.getContentType(),
//                    file.getSize()
//            );
//        } catch (Exception e) {
//            throw new RuntimeException("Could not upload file to MinIO", e);
//        }
//    }
//
//    public StoredBytes getFileBytes(String objectName, String contentType) {
//        if (objectName == null || objectName.isBlank()) {
//            throw new IllegalArgumentException("Object name is required");
//        }
//
//        try (InputStream inputStream = minioClient.getObject(
//                GetObjectArgs.builder()
//                        .bucket(bucketName)
//                        .object(objectName)
//                        .build()
//        )) {
//            byte[] bytes = inputStream.readAllBytes();
//
//            return new StoredBytes(
//                    bytes,
//                    contentType != null ? contentType : "application/octet-stream"
//            );
//        } catch (Exception e) {
//            throw new RuntimeException("Could not read file from MinIO", e);
//        }
//    }
//
//    public void deleteFile(String objectName) {
//        if (objectName == null || objectName.isBlank()) {
//            return;
//        }
//
//        try {
//            minioClient.removeObject(
//                    RemoveObjectArgs.builder()
//                            .bucket(bucketName)
//                            .object(objectName)
//                            .build()
//            );
//        } catch (Exception e) {
//            throw new RuntimeException("Could not delete file from MinIO", e);
//        }
//    }
//
//    private void ensureBucketExists() {
//        try {
//            boolean exists = minioClient.bucketExists(
//                    BucketExistsArgs.builder()
//                            .bucket(bucketName)
//                            .build()
//            );
//
//            if (!exists) {
//                minioClient.makeBucket(
//                        MakeBucketArgs.builder()
//                                .bucket(bucketName)
//                                .build()
//                );
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("Could not initialize MinIO bucket", e);
//        }
//    }
//
//    private void validateFile(MultipartFile file) {
//        if (file == null || file.isEmpty()) {
//            throw new IllegalArgumentException("File is required");
//        }
//
//        if (file.getContentType() == null ||
//                !ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
//            throw new IllegalArgumentException("Unsupported file type");
//        }
//
//        long maxSize = 5 * 1024 * 1024;
//
//        if (file.getSize() > maxSize) {
//            throw new IllegalArgumentException("File is too large");
//        }
//    }
//
//    private String extractExtension(String filename) {
//        if (filename == null || filename.isBlank()) {
//            return ".bin";
//        }
//
//        int lastDot = filename.lastIndexOf('.');
//        if (lastDot < 0 || lastDot == filename.length() - 1) {
//            return ".bin";
//        }
//
//        return filename.substring(lastDot).toLowerCase(Locale.ROOT);
//    }
//
//    public record StoredFile(
//            String objectName,
//            String contentType,
//            long size
//    ) {
//    }
//
//    public record StoredBytes(
//            byte[] bytes,
//            String contentType
//    ) {
//    }
//}

package org.example.sumoney.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "application/pdf"
    );

    private final Cloudinary cloudinary;
    private final String folder;

    public FileStorageService(
            Cloudinary cloudinary,
            @Value("${cloudinary.folder}") String folder
    ) {
        this.cloudinary = cloudinary;
        this.folder = folder;
    }

    public StoredFile uploadReceipt(MultipartFile file) {
        validateFile(file);

        try {
            String publicId = folder + "/" + UUID.randomUUID();

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "resource_type", "auto",
                            "overwrite", false
                    )
            );

            String uploadedPublicId = (String) uploadResult.get("public_id");

            return new StoredFile(
                    uploadedPublicId,
                    file.getContentType()
            );
        } catch (Exception e) {
            throw new RuntimeException("Could not upload file to Cloudinary", e);
        }
    }

    public String getFileUrl(String objectName, String contentType) {
        if (objectName == null || objectName.isBlank()) {
            throw new IllegalArgumentException("Receipt not found");
        }

        String resourceType = resolveResourceType(contentType);

        return cloudinary.url()
                .secure(true)
                .resourceType(resourceType)
                .generate(objectName);
    }

    public void deleteFile(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            return;
        }

        try {
            cloudinary.uploader().destroy(
                    objectName,
                    ObjectUtils.asMap("resource_type", "image")
            );

            cloudinary.uploader().destroy(
                    objectName,
                    ObjectUtils.asMap("resource_type", "raw")
            );
        } catch (Exception e) {
            throw new RuntimeException("Could not delete file from Cloudinary", e);
        }
    }

    private String resolveResourceType(String contentType) {
        if ("application/pdf".equals(contentType)) {
            return "raw";
        }

        return "image";
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        if (file.getContentType() == null ||
                !ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Unsupported file type");
        }

        long maxSize = 5 * 1024 * 1024;

        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File is too large");
        }
    }

    public record StoredFile(
            String objectName,
            String contentType
    ) {
    }
}