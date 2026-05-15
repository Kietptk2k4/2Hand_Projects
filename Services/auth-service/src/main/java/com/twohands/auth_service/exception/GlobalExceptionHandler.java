package com.twohands.auth_service.exception;

import com.twohands.auth_service.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiResponse.ApiError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toApiError)
                .toList();

        return buildApi(ErrorCode.VALIDATION_ERROR.status(), ErrorCode.VALIDATION_ERROR.defaultMessage(), errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return buildApi(HttpStatus.BAD_REQUEST, "Invalid request payload", null);
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        List<ApiResponse.ApiError> errors = null;
        if (ex.getField() != null && ex.getReason() != null) {
            errors = List.of(new ApiResponse.ApiError(ex.getField(), ex.getReason()));
        }
        return buildApi(errorCode.status(), ex.getMessage(), errors);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        return buildApi(ErrorCode.UNAUTHORIZED.status(), "Invalid username or password", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return buildApi(ErrorCode.FORBIDDEN.status(), ErrorCode.FORBIDDEN.defaultMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception ex) {
        return buildApi(ErrorCode.INTERNAL_ERROR.status(), ErrorCode.INTERNAL_ERROR.defaultMessage(), null);
    }

    private ResponseEntity<ApiResponse<Void>> buildApi(
            HttpStatus status,
            String message,
            List<ApiResponse.ApiError> errors
    ) {
        ApiResponse<Void> response = new ApiResponse<>(
                status.value(),
                false,
                message,
                null,
                errors,
                Instant.now()
        );
        return ResponseEntity.status(status).body(response);
    }

    private ApiResponse.ApiError toApiError(FieldError fieldError) {
        return new ApiResponse.ApiError(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
