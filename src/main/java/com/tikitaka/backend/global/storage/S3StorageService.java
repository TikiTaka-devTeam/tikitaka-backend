package com.tikitaka.backend.global.storage;

import java.io.IOException;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class S3StorageService {

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            "jpg",
            "jpeg",
            "png",
            "gif",
            "webp"
    );

    private static final String LECTURE_PDF_PREFIX = "files/lecture-pdfs";
    private static final String LECTURE_SLIDE_PREFIX = "files/lecture-slides";

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties properties;

    public PresignedUpload createProfileImageUpload(String originalFilename, String contentType) {
        validateImageContentType(contentType);

        String extension = extractAllowedImageExtension(originalFilename);
        String objectKey = properties.profilePrefix() + "/" + UUID.randomUUID() + "." + extension;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(properties.presignedUrlExpirationMinutes()))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return new PresignedUpload(
                presignedRequest.url().toString(),
                objectKey,
                toCloudFrontUrl(objectKey),
                properties.presignedUrlExpirationMinutes() * 60
        );
    }

    public String uploadLecturePdf(String documentFileKey, String originalFilename, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PDF 파일은 필수입니다.");
        }

        String safeFilename = sanitizeFilename(originalFilename);
        String objectKey = LECTURE_PDF_PREFIX + "/" + documentFileKey + "/" + safeFilename;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(objectKey)
                    .contentType("application/pdf")
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            return toCloudFrontUrl(objectKey);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "PDF 파일을 S3에 업로드하는 중 오류가 발생했습니다."
            );
        }
    }

    public String uploadLectureSlideImage(
            String documentFileKey,
            String imageFileName,
            byte[] imageBytes,
            String contentType
    ) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "슬라이드 이미지 데이터가 비어 있습니다.");
        }

        String safeImageFileName = sanitizeFilename(imageFileName);
        String objectKey = LECTURE_SLIDE_PREFIX + "/" + documentFileKey + "/" + safeImageFileName;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(objectKey)
                .contentType(contentType)
                .contentLength((long) imageBytes.length)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(imageBytes));

        return toCloudFrontUrl(objectKey);
    }

    public void deleteObjectIfExists(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }

        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(properties.bucket())
                .key(objectKey)
                .build());
    }

    public void validateProfileObjectKey(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "프로필 이미지 object key는 필수입니다.");
        }

        String allowedPrefix = properties.profilePrefix() + "/";
        if (!objectKey.startsWith(allowedPrefix) || objectKey.contains("..")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "프로필 이미지 object key가 올바르지 않습니다.");
        }
    }

    public String toCloudFrontUrl(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }

        String domain = properties.cloudfrontDomain();
        if (domain == null || domain.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "CloudFront 도메인이 설정되지 않았습니다.");
        }

        String normalizedDomain = domain.endsWith("/")
                ? domain.substring(0, domain.length() - 1)
                : domain;

        String normalizedKey = objectKey.startsWith("/")
                ? objectKey.substring(1)
                : objectKey;

        return normalizedDomain + "/" + normalizedKey;
    }

    private void validateImageContentType(String contentType) {
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 파일만 업로드할 수 있습니다.");
        }
    }

    private String extractAllowedImageExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일 이름이 올바르지 않습니다.");
        }

        String filename = originalFilename.substring(originalFilename.lastIndexOf('/') + 1);
        int dotIndex = filename.lastIndexOf('.');

        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 파일 확장자가 필요합니다.");
        }

        String extension = filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);

        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원하지 않는 이미지 형식입니다.");
        }

        return extension;
    }

    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return UUID.randomUUID().toString();
        }

        String onlyFilename = filename.substring(filename.lastIndexOf('/') + 1)
                .substring(filename.lastIndexOf('\\') + 1);

        return onlyFilename
                .replaceAll("[^a-zA-Z0-9._가-힣-]", "_")
                .replace("..", "_");
    }

    public record PresignedUpload(
            String uploadUrl,
            String objectKey,
            String profileUrl,
            long expiresInSeconds
    ) {
    }
}