// src/main/java/com/co/gestiondecitasmedicas/models/Cita.java
package com.co.gestiondecitasmedicas.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "citas")
public class Cita {

    public enum Estado {
        RESERVADA,
        CANCELADA,
        REALIZADA,
        FACTURADA     // ← nuevo estado
        
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Fecha y hora de la cita
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    // Estado (RESERVADA, CANCELADA, REALIZADA)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Estado estado = Estado.RESERVADA;

    // Paciente que reservó la cita
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Usuario paciente;

    // Médico asignado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", nullable = false)
    private Usuario medico;

    // Clínica (se almacena para facilitar búsquedas)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinica_id", nullable = false)
    private Clinica clinica;

    // Relación 1:1 con el historial (solo si la cita se realizó)
    @OneToOne(mappedBy = "cita", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private HistorialMedico historial;

    
    @Column(nullable = false, length = 200)
    private String motivo;

    @Column(nullable = false, length = 100)
    private String correoContacto;
    
 // ← NUEVOS CAMPOS
    @Column(name = "paciente_nombre", nullable = false, length = 100)
    private String pacienteNombre;

    @Column(name = "medico_nombre",   nullable = false, length = 100)
    private String medicoNombre;
    
    @Column(name = "clinica_nombre",   nullable = false, length = 100)
    private String clinicaNombre;
    
    @Column(name = "documento_identidad",   nullable = false, length = 100)
    private String documento;
    
    @Column(name = "valor_pagar", precision = 12, scale = 2)
    private BigDecimal valorPagar;


    // Getters y setters...

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public Usuario getPaciente() { return paciente; }
    public void setPaciente(Usuario paciente) { this.paciente = paciente; }

    public Usuario getMedico() { return medico; }
    public void setMedico(Usuario medico) { this.medico = medico; }

    public Clinica getClinica() { return clinica; }
    public void setClinica(Clinica clinica) { this.clinica = clinica; }

    public HistorialMedico getHistorial() { return historial; }
    public void setHistorial(HistorialMedico historial) { 
        this.historial = historial; 
        if (historial != null) {
            historial.setCita(this);
        }
    }
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
	
	
    public String getPacienteNombre() { return pacienteNombre; }
    public void setPacienteNombre(String pacienteNombre) {
        this.pacienteNombre = pacienteNombre;
    }

    public String getMedicoNombre() { return medicoNombre; }
    public void setMedicoNombre(String medicoNombre) {
        this.medicoNombre = medicoNombre;
    }
    
    
	public String getClinicaNombre() {
		return clinicaNombre;
	}
	public void setClinicaNombre(String clinicaNombre) {
		this.clinicaNombre = clinicaNombre;
	}
	public String getDocumento() {
		return documento;
	}
	public void setDocumento(String documento) {
		this.documento = documento;
	}

	  public BigDecimal getValorPagar() { return valorPagar; }
	    public void setValorPagar(BigDecimal valorPagar) { this.valorPagar = valorPagar; }
    
    
}
