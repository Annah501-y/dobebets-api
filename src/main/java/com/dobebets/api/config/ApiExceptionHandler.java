package com.dobebets.api.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Converts expected business-rule failures into useful API responses.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRequest(
            IllegalArgumentException exception,
            HttpServletRequest request) {

        return buildResponse(HttpStatus.BAD_REQUEST, exception, request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidState(
            IllegalStateException exception,
            HttpServletRequest request) {

        return buildResponse(HttpStatus.CONFLICT, exception, request);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            RuntimeException exception,
            HttpServletRequest request) {

        ApiErrorResponse body = new ApiErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                status.value(),
                status.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(status).body(body);
    }

    /**
     * Provides a clear explanation when a request violates a business rule.
     */
    public record ApiErrorResponse(
            OffsetDateTime timestamp,
            int status,
            String error,
            String message,
            String path) {}
}