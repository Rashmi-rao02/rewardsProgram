package com.retailer.reward.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.validation.ConstraintViolationException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErrorDetails(LocalDateTime timestamp, int status, String error, String message) {}


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(" | "));

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error", message);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDetails> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining(" | "));
        return buildResponse(HttpStatus.BAD_REQUEST, "Parameter Error", message);
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetails> handleBusinessLogicErrors(IllegalArgumentException ex) {
        log.warn("Business logic violation: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error", ex.getMessage());
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDetails> handleMalformedJson(HttpMessageNotReadableException ex) {
        log.warn("Malformed JSON: {}", ex.getMessage());
        String message = "requestBody: Malformed JSON or invalid data types (Check date format YYYY-MM-DD)";
        return buildResponse(HttpStatus.BAD_REQUEST, "Malformed Request", message);
    }


    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorDetails> handleParamErrors(Exception ex) {
        String source = (ex instanceof MethodArgumentTypeMismatchException) ? "typeMismatch" : "missingParam";
        return buildResponse(HttpStatus.BAD_REQUEST, "Parameter Error", source + ": " + ex.getMessage());
    }


    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorDetails> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "Method Error", "httpMethod: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGeneralException(Exception ex) {
        log.error("Unhandled server error: ", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", "system: An unexpected error occurred.");
    }

    private ResponseEntity<ErrorDetails> buildResponse(HttpStatus status, String errorLabel, String message) {
        ErrorDetails error = new ErrorDetails(LocalDateTime.now(), status.value(), errorLabel, message);
        return new ResponseEntity<>(error, status);
    }
}