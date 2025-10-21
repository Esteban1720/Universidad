// src/main/java/com/co/gestiondecitasmedicas/dto/HistorialDto.java
package com.co.gestiondecitasmedicas.dto;

public class HistorialDto {
    private String diagnostico;
    private String receta;

    // Getters y setters
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public String getReceta() { return receta; }
    public void setReceta(String receta) { this.receta = receta; }
}
