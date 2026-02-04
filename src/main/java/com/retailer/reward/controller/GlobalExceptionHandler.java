package com.retailer.reward.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErrorDetails(LocalDateTime timestamp, int status, String error, String message) {}



    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDetails handleMissingParams(org.springframework.web.bind.MissingServletRequestParameterException ex) {
        String message = String.format("The required query parameter '%s' is missing from the URL.", ex.getParameterName());

        log.warn("Missing parameter in request: {}", ex.getParameterName());

        return new ErrorDetails(
                LocalDateTime.now(),
                400,
                "Missing Parameter",
                message
        );
    }


    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            org.springframework.web.bind.MethodArgumentNotValidException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDetails handleClientErrors(Exception ex) {

        log.warn("Client error occurred: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
        String message;

        if (ex instanceof HttpMessageNotReadableException) {
            message = "Invalid input: customerId must be a number and date must be YYYY-MM-DD.";
        } else if (ex instanceof org.springframework.web.bind.MethodArgumentNotValidException) {
            message = "Missing required fields: Ensure customerId is provided.";
        } else {
            message = ex.getMessage();
        }
        return new ErrorDetails(LocalDateTime.now(), 400, "Bad Request", message);
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDetails handleGeneralException(Exception ex) {
        log.error("Unhandled server error: ", ex);
        return new ErrorDetails(LocalDateTime.now(), 500, "Internal Server Error", "An unexpected error occurred.");
    }
}
