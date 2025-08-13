package com.khmall.common.constants;

import java.util.List;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ImageConstants {

  // 이미지 형식
  public static final List<String> ALLOWED_IMAGE_FORMATS = List.of(
      "image/jpeg", // JPEG
      "image/png",  // PNG
      "image/webp"  // WebP
  );

  // 캐시 제어
  public static final String CACHE_CONTROL = "public, max-age=31536000, immutable";

  // 용량 제한
  public static final long MAX_SIZE = 10L * 1024 * 1024; // 10MB

  // error 메시지
  public static final String SIZE_EXCEEDED = "이미지 크기 초과(최대 10MB)";
  public static final String IMAGE_NOT_REGISTERED = "이미지 등록 후 상품을 등록해야합니다.";
  public static final String UNSUPPORTED_FORMAT = "허용되지 않는 이미지 형식입니다. (jpeg, png, webp만 허용)";
  public static final String EMPTY_IMAGE = "이미지 파일이 비어있습니다. 올바른 이미지를 업로드해주세요.";

}
