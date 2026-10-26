package com.codecrafthub.controller;

import com.codecrafthub.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejador centralizado de errores de la API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja errores de validación como:
     * - name vacío
     * - description vacío
     * - target_date ausente
     * - status ausente
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        fieldErrors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        Map<String, Object> response = createBaseResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación"
        );

        response.put("errors", fieldErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Maneja cursos inexistentes.
     */
    @ExceptionHandler(CourseService.CourseNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFoundException(
            CourseService.CourseNotFoundException exception
    ) {
        Map<String, Object> response = createBaseResponse(
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    /**
     * Maneja estados inválidos.
     */
    @ExceptionHandler(CourseService.InvalidStatusException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidStatusException(
            CourseService.InvalidStatusException exception
    ) {
        Map<String, Object> response = createBaseResponse(
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Maneja problemas de lectura o escritura del archivo.
     */
    @ExceptionHandler(CourseService.FileStorageException.class)
    public ResponseEntity<Map<String, Object>> handleFileStorageException(
            CourseService.FileStorageException exception
    ) {
        Map<String, Object> response = createBaseResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    /**
     * Maneja errores de formato, por ejemplo:
     * - ID no numérico
     * - Fecha con formato incorrecto
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception exception
    ) {
        Map<String, Object> response = createBaseResponse(
                HttpStatus.BAD_REQUEST.value(),
                "La solicitud no tiene un formato válido"
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    private Map<String, Object> createBaseResponse(
            int status,
            String message
    ) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", Instant.now());
        response.put("status", status);
        response.put("message", message);

        return response;
    }
}