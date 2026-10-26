package com.codecrafthub.controller;

import com.codecrafthub.model.Course;
import com.codecrafthub.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para cursos.
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * POST /api/courses
     *
     * Crea un nuevo curso.
     */
    @PostMapping
    public ResponseEntity<Course> createCourse(
            @Valid @RequestBody Course course
    ) {
        Course createdCourse = courseService.createCourse(course);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdCourse);
    }

    /**
     * GET /api/courses
     *
     * Obtiene todos los cursos.
     */
    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(
                courseService.getAllCourses()
        );
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCourseStats() {
        return ResponseEntity.ok(courseService.getCourseStats());
    }

    /**
     * GET /api/courses/{id}
     *
     * Obtiene un curso específico.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                courseService.getCourseById(id)
        );
    }

    /**
     * PUT /api/courses/{id}
     *
     * Actualiza un curso existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody Course course
    ) {
        return ResponseEntity.ok(
                courseService.updateCourse(id, course)
        );
    }

    /**
     * DELETE /api/courses/{id}
     *
     * Elimina un curso.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable Long id
    ) {
        courseService.deleteCourse(id);

        return ResponseEntity.noContent().build();
    }

}