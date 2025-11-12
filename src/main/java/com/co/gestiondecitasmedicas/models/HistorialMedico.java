// src/main/java/com/co/gestiondecitasmedicas/models/HistorialMedico.java
package com.co.gestiondecitasmedicas.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "historiales_medicos")
public class HistorialMedico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Relación 1:1 con Cita (única, obligatoria)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id", nullable = false, unique = true)
    private Cita cita;

    // Fecha en que se guarda el historial
    @Column(name = "fecha_realizacion", nullable = false)
    private LocalDateTime fechaRealizacion = LocalDateTime.now();

    // Texto con diagnóstico
    @Column(columnDefinition = "TEXT", nullable = false)
    private String diagnostico;

    // Texto con fórmula o medicamentos
    @Column(columnDefinition = "TEXT", nullable = false)
    private String receta;

    // Getters y setters...

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Cita getCita() { return cita; }
    public void setCita(Cita cita) { this.cita = cita; }

    public LocalDateTime getFechaRealizacion() { return fechaRealizacion; }
    public void setFechaRealizacion(LocalDateTime fechaRealizacion) { this.fechaRealizacion = fechaRealizacion; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public String getReceta() { return receta; }
    public void setReceta(String receta) { this.receta = receta; }
}
