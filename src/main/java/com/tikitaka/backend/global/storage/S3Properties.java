package com.tikitaka.backend.global.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.aws.s3")
public record S3Properties(
    String bucket,
    String region,
    String cloudfrontDomain,
    String profilePrefix,
    long presignedUrlExpirationMinutes,
    String accessKeyId,
    String secretAccessKey
) {
    public S3Properties {
        if (profilePrefix == null || profilePrefix.isBlank()) {
            profilePrefix = "files/profiles";
        }
        if (presignedUrlExpirationMinutes <= 0) {
            presignedUrlExpirationMinutes = 10;
        }
    }
}
