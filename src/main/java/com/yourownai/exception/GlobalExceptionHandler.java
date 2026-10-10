package com.yourownai.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Provides GlobalExceptionHandler behavior.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Executes validation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "Validation failed", "details", e.getBindingResult().getFieldErrors().stream().map(x -> x.getField() + ": " + x.getDefaultMessage()).toList()));
    }

    /**
     * Executes illegal.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<?> illegal(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    /**
     * Executes state.
     */
    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<?> state(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", e.getMessage()));
    }

    /**
     * Executes notFound.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<?> notFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }
}
