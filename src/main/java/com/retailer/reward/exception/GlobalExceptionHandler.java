package com.retailer.reward.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErrorDetails(LocalDateTime timestamp, int status, String error, String message) {}

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorDetails> handleMissingParams(MissingServletRequestParameterException ex) {
        String message = String.format("The required query parameter '%s' is missing from the URL.", ex.getParameterName());

        log.warn("Missing parameter in request: {}", ex.getParameterName());

        ErrorDetails error = new ErrorDetails(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                "Missing Parameter",
                message
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorDetails> handleMethodValidationException(HandlerMethodValidationException ex) {
        String message = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream())
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Method parameter validation failed: {}", message);

        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                message
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class
    })
    public ResponseEntity<ErrorDetails> handleClientErrors(Exception ex) {
        log.warn("Client error occurred: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
        String message;

        if (ex instanceof HttpMessageNotReadableException) {
            message = "Malformed JSON input: Check data types and Date format (YYYY-MM-DD).";
        }
        else if (ex instanceof MethodArgumentNotValidException validEx) {
            // Extract all field-specific @NotNull messages
            List<FieldError> fieldErrors = validEx.getBindingResult().getFieldErrors();
            message = fieldErrors.stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
        }
        else if (ex instanceof MethodArgumentTypeMismatchException mismatchEx) {
            message = String.format("Parameter '%s' has an invalid value.", mismatchEx.getName());
        }
        else {
            message = ex.getMessage();
        }

        ErrorDetails error = new ErrorDetails(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                "Bad Request", message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGeneralException(Exception ex) {
        log.error("Unhandled server error: ", ex);

        ErrorDetails error = new ErrorDetails(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred."
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
