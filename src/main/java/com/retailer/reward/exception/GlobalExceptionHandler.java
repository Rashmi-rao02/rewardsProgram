package com.retailer.reward.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    public record ErrorDetails(LocalDateTime timestamp, int status, String error, String message) {}

    // VALIDATION ERRORS (@Valid, @NotEmpty, @NotNull)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Validation failed for request: {}", message);
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Failed", message);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorDetails> handleMethodValidationException(HandlerMethodValidationException ex) {
        String message = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream())
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Method parameter validation failed: {}", message);
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Failed", message);
    }

    //JSON & TYPE ERRORS (Malformed JSON, Wrong Data Types)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDetails> handleMalformedJson(HttpMessageNotReadableException ex) {
        log.warn("Malformed JSON received: {}", ex.getMessage());
        String message = "Malformed JSON input: customerId must be a number and Date format (YYYY-MM-DD).";
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDetails> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Parameter '%s' has an invalid value.", ex.getName());
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", message);
    }

    //URL & PARAMETER ERRORS
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorDetails> handleMissingParams(MissingServletRequestParameterException ex) {
        String message = String.format("The required query parameter '%s' is missing from the URL.", ex.getParameterName());
        return buildResponse(HttpStatus.BAD_REQUEST, "Missing Parameter", message);
    }

    //BUSINESS LOGIC ERRORS
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetails> handleBusinessLogicErrors(IllegalArgumentException ex) {
        log.warn("Business logic violation: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGeneralException(Exception ex) {
        log.error("Unhandled server error: ", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred.");
    }


    private ResponseEntity<ErrorDetails> buildResponse(HttpStatus status, String errorLabel, String message) {
        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                status.value(),
                errorLabel,
                message
        );
        return new ResponseEntity<>(error, status);
    }
}