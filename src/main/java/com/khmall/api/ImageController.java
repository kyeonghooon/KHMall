package com.khmall.api;

import com.khmall.common.constants.ImageConstants;
import com.khmall.config.S3Props;
import com.khmall.domain.product.dto.ImagePresignUrlResponse;
import com.khmall.exception.custom.BadRequestException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/images")
public class ImageController {

  private final S3Client s3;
  private final S3Props props;
  private final S3Presigner presigner;

  // presign URL 생성 로직
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/presign")
  public ResponseEntity<ImagePresignUrlResponse> presign(
      @RequestParam String contentType
  ) {
    String key = buildKeyWithContentType(contentType);

    final PresignedPutObjectRequest presigned = presigner.presignPutObject(b -> b
        .signatureDuration(Duration.ofMinutes(5))
        .putObjectRequest(p -> p
            .bucket(props.getBucket())
            .key(key)
            .contentType(contentType)
            .cacheControl(ImageConstants.CACHE_CONTROL)
        )
    );

    return ResponseEntity.ok(
        new ImagePresignUrlResponse(
            key,
            contentType,
            presigned.url().toString(),
            ImageConstants.CACHE_CONTROL
        )
    );
  }

  private String buildKeyWithContentType(String contentType) {
    String ct = (contentType == null) ? "" : contentType.toLowerCase();
    if (!ImageConstants.ALLOWED_IMAGE_FORMATS.contains(ct)) {
      throw new BadRequestException(ImageConstants.UNSUPPORTED_FORMAT);
    }
    String ext = switch (ct) {
      case "image/png" -> ".png";
      case "image/jpeg" -> ".jpg";
      case "image/webp" -> ".webp";
      default -> "";
    };

    LocalDate now = LocalDate.now();
    String y = String.valueOf(now.getYear());
    String m = String.format("%02d", now.getMonthValue());
    String uuid = UUID.randomUUID().toString().replace("-", "");
    return props.getPrefix() + "/" + y + "/" + m + "/" + uuid + ext;
  }

}
