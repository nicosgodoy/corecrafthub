package com.codecrafthub.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Modelo que representa un curso.
 *
 * Los nombres internos usan camelCase, pero Jackson los convierte
 * a los nombres definidos en las anotaciones @JsonProperty.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Course {

    /**
     * El ID se genera automáticamente en el servicio.
     * READ_ONLY evita que el cliente lo envíe o modifique.
     */
    @JsonProperty(value = "id")
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @JsonProperty("name")
    private String name;

    @NotBlank(message = "La descripción es obligatoria")
    @JsonProperty("description")
    private String description;

    @NotNull(message = "La fecha objetivo es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("target_date")
    private LocalDate targetDate;

    @NotBlank(message = "El estado es obligatorio")
    @JsonProperty("status")
    private String status;

    /**
     * Se genera automáticamente al crear el curso.
     *
     * No debe ser READ_ONLY: el servicio necesita poder deserializarlo
     * al leer courses.json; de lo contrario la fecha se pierde en cada
     * reescritura del archivo. El servicio ignora cualquier valor enviado
     * por el cliente y siempre asigna Instant.now() al crear.
     */
    @JsonProperty("created_at")
    private Instant createdAt;

    public Course() {
    }

    public Course(
            Long id,
            String name,
            String description,
            LocalDate targetDate,
            String status,
            Instant createdAt
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.targetDate = targetDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}