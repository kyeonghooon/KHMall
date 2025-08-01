package com.khmall.exception;

public record ErrorResponse(
    int status,
    String message
) {

}
