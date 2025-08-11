package com.khmall.domain.product.dto;

public record ImagePresignUrlResponse(
    String key,
    String contentType,
    String uploadUrl,
    String cacheControl
) {

}
