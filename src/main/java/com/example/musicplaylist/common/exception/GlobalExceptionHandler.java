package com.example.musicplaylist.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Handles business rule violations
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {
        HttpStatus status = resolveHttpStatus(exception.getErrorCode());

        return buildErrorResponse(status,
                exception.getErrorCode(),
                exception.getMessage()
        );
    }

    // Handles request DTO validation failures
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));

        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;

        ErrorResponse response = new ErrorResponse(
                status.value(),
                errorCode,
                message.isBlank() ? errorCode.getMessage() : message
        );

        return super.handleExceptionInternal(exception, response, headers, status, request);
    }

    // Handles standard Spring MVC exceptions
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        boolean serverError = status.is5xxServerError();

        if (serverError) {
            log.error("Unexpected MVC error occurred", exception);
        }

        ErrorCode errorCode = serverError
                ? ErrorCode.INTERNAL_SERVER_ERROR
                : ErrorCode.INVALID_REQUEST;

        ErrorResponse response = new ErrorResponse(
                status.value(),
                errorCode,
                errorCode.getMessage()
        );

        return super.handleExceptionInternal(exception, response, headers, status, request);
    }

    // Handles unexpected exceptions not covered by other handlers
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception) {
        log.error("Unexpected error occurred", exception);

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                errorCode,
                errorCode.getMessage()
        );
    }

    private HttpStatus resolveHttpStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case INVALID_REQUEST, VALIDATION_FAILED, INVALID_SONG, INVALID_POSITION, INVALID_SHUFFLE_STRATEGY, INVALID_EXPORT_FORMAT -> HttpStatus.BAD_REQUEST;
            case PLAYLIST_NOT_FOUND, SONG_NOT_FOUND, SONG_NOT_IN_PLAYLIST -> HttpStatus.NOT_FOUND;
            case DUPLICATE_SONG -> HttpStatus.CONFLICT;
            case INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, ErrorCode errorCode, String message) {
        ErrorResponse response = new ErrorResponse(
                status.value(),
                errorCode,
                message
        );

        return ResponseEntity.status(status).body(response);
    }
}