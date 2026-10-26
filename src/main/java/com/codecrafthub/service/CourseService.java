package com.codecrafthub.service;

import com.codecrafthub.model.Course;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;     // <-- Agrega esta línea
import java.util.HashMap;

/**
 * Capa de servicio.
 *
 * Esta clase contiene:
 * - La lógica de negocio.
 * - La lectura del archivo courses.json.
 * - La escritura del archivo courses.json.
 */
@Service
public class CourseService {

    private static final Path FILE_PATH = Paths.get("courses.json");

    private static final List<String> VALID_STATUSES = List.of(
            "Not Started",
            "In Progress",
            "Completed"
    );

    private final ObjectMapper objectMapper;

    public CourseService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Obtiene todos los cursos.
     */
    public synchronized List<Course> getAllCourses() {
        return readCoursesFromFile();
    }

    /**
     * Busca un curso por su ID.
     */
    public synchronized Course getCourseById(Long id) {
        return readCoursesFromFile()
                .stream()
                .filter(course -> course.getId().equals(id))
                .findFirst()
                .orElseThrow(() ->
                        new CourseNotFoundException("No se encontró el curso con ID: " + id)
                );
    }

    /**
     * Crea un nuevo curso.
     *
     * El ID se genera tomando el ID más alto y sumando 1.
     */
    public synchronized Course createCourse(Course course) {
        validateStatus(course.getStatus());

        List<Course> courses = readCoursesFromFile();

        long nextId = courses.stream()
                .map(Course::getId)
                .filter(id -> id != null)
                .max(Comparator.naturalOrder())
                .orElse(0L) + 1;

        course.setId(nextId);
        course.setCreatedAt(Instant.now());

        courses.add(course);
        writeCoursesToFile(courses);

        return course;
    }

    /**
     * Actualiza un curso existente.
     *
     * El ID y createdAt originales no se modifican.
     */
    public synchronized Course updateCourse(Long id, Course updatedCourse) {
        validateStatus(updatedCourse.getStatus());

        List<Course> courses = readCoursesFromFile();

        Course existingCourse = courses.stream()
                .filter(course -> course.getId().equals(id))
                .findFirst()
                .orElseThrow(() ->
                        new CourseNotFoundException("No se encontró el curso con ID: " + id)
                );

        existingCourse.setName(updatedCourse.getName());
        existingCourse.setDescription(updatedCourse.getDescription());
        existingCourse.setTargetDate(updatedCourse.getTargetDate());
        existingCourse.setStatus(updatedCourse.getStatus());

        writeCoursesToFile(courses);

        return existingCourse;
    }

    /**
     * Elimina un curso por su ID.
     */
    public synchronized void deleteCourse(Long id) {
        List<Course> courses = readCoursesFromFile();

        boolean removed = courses.removeIf(course ->
                course.getId().equals(id)
        );

        if (!removed) {
            throw new CourseNotFoundException(
                    "No se encontró el curso con ID: " + id
            );
        }

        writeCoursesToFile(courses);
    }

    /**
     * Valida que el estado sea uno de los valores permitidos.
     */
    private void validateStatus(String status) {
        if (!VALID_STATUSES.contains(status)) {
            throw new InvalidStatusException(
                    "El estado debe ser exactamente uno de estos valores: "
                            + String.join(", ", VALID_STATUSES)
            );
        }
    }

    /**
     * Lee cursos desde courses.json.
     *
     * Si el archivo no existe, lo crea automáticamente
     * con un array JSON vacío.
     */
    private List<Course> readCoursesFromFile() {
        try {
            createFileIfNotExists();

            String json = Files.readString(
                    FILE_PATH,
                    StandardCharsets.UTF_8
            );

            // Si el archivo está vacío, lo tratamos como una lista vacía.
            if (json == null || json.isBlank()) {
                return new ArrayList<>();
            }

            List<Course> courses = objectMapper.readValue(
                    json,
                    new TypeReference<List<Course>>() {
                    }
            );

            return courses != null ? courses : new ArrayList<>();

        } catch (JsonProcessingException exception) {
            throw new FileStorageException(
                    "El archivo courses.json contiene JSON inválido",
                    exception
            );
        } catch (IOException exception) {
            throw new FileStorageException(
                    "No se pudo leer el archivo courses.json",
                    exception
            );
        }
    }

    /**
     * Guarda todos los cursos en courses.json.
     */
    private void writeCoursesToFile(List<Course> courses) {
        try {
            createFileIfNotExists();

            String json = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(courses);

            Files.writeString(
                    FILE_PATH,
                    json,
                    StandardCharsets.UTF_8
            );

        } catch (JsonProcessingException exception) {
            throw new FileStorageException(
                    "No se pudo convertir la información a JSON",
                    exception
            );
        } catch (IOException exception) {
            throw new FileStorageException(
                    "No se pudo escribir el archivo courses.json",
                    exception
            );
        }
    }

    /**
     * Crea el archivo si todavía no existe.
     */
    private void createFileIfNotExists() throws IOException {
        if (!Files.exists(FILE_PATH)) {
            Files.writeString(
                    FILE_PATH,
                    "[]",
                    StandardCharsets.UTF_8
            );
        }
    }

    /**
     * Excepción para cursos inexistentes.
     */
    public static class CourseNotFoundException extends RuntimeException {
        public CourseNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Excepción para estados no permitidos.
     */
    public static class InvalidStatusException extends RuntimeException {
        public InvalidStatusException(String message) {
            super(message);
        }
    }

    /**
     * Excepción para errores de lectura o escritura.
     */
    public static class FileStorageException extends RuntimeException {
        public FileStorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Calcula estadísticas generales de los cursos.
     */
    public synchronized Map<String, Object> getCourseStats() {
        List<Course> courses = readCoursesFromFile();

        long totalCourses = courses.size();

        long notStartedCount = courses.stream()
                .filter(c -> "Not Started".equals(c.getStatus()))
                .count();

        long inProgressCount = courses.stream()
                .filter(c -> "In Progress".equals(c.getStatus()))
                .count();

        long completedCount = courses.stream()
                .filter(c -> "Completed".equals(c.getStatus()))
                .count();

        Map<String, Long> statusCounts = Map.of(
                "Not Started", notStartedCount,
                "In Progress", inProgressCount,
                "Completed", completedCount
        );

        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("total_courses", totalCourses);
        stats.put("status_counts", statusCounts);

        return stats;
    }
}