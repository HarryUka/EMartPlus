package com.emartplus.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.Data;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        return new ResponseEntity<>(
            new ErrorResponse(ex.getMessage()),
            ex.getStatus()
        );
    }

    @Data
    static class ErrorResponse {
        private final String message;
    }
} 