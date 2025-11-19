// src/main/java/com/co/gestiondecitasmedicas/dto/CitaDto.java
package com.co.gestiondecitasmedicas.dto;

/**
 * DTO (Data Transfer Object) utilizado para recibir desde el formulario web
 * la información necesaria para crear una cita médica.
 *
 * Este objeto NO representa la entidad completa de la base de datos, solo
 * contiene los datos que el usuario ingresa en el formulario.
 *
 * Campos principales:
 *  - clinicaId:       ID de la clínica seleccionada por el paciente.
 *  - medicoId:        ID del médico elegido.
 *  - fechaHora:       Fecha y hora de la cita en formato "yyyy-MM-dd'T'HH:mm",
 *                     proveniente de un input <input type="datetime-local">.
 *
 * Campos adicionales agregados:
 *  - motivo:          Descripción breve del motivo o razón de la consulta.
 *  - correoContacto:  Correo electrónico donde se enviará confirmación o avisos.
 */

public class CitaDto {
    private Integer clinicaId;  // id de la clínica seleccionada
    private Integer medicoId;   // id del médico elegido
    // Recibiremos la fecha desde un <input type="datetime-local"> (formato "yyyy-MM-dd'T'HH:mm")
    private String fechaHora;
    
    // ⬇ nuevos campos
    private String motivo;
    private String correoContacto;

    public String getMotivo() {
		return motivo;
	}
	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}
	public String getCorreoContacto() {
		return correoContacto;
	}
	public void setCorreoContacto(String correoContacto) {
		this.correoContacto = correoContacto;
	}
	// Getters y setters
    public Integer getClinicaId() { return clinicaId; }
    public void setClinicaId(Integer clinicaId) { this.clinicaId = clinicaId; }

    public Integer getMedicoId() { return medicoId; }
    public void setMedicoId(Integer medicoId) { this.medicoId = medicoId; }

    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }
}
