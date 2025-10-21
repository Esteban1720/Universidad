// src/main/java/com/co/gestiondecitasmedicas/repository/CitaRepository.java
package com.co.gestiondecitasmedicas.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.co.gestiondecitasmedicas.models.Cita;

public interface CitaRepository extends JpaRepository<Cita, Integer> {

    // Listar todas las citas de un paciente
    List<Cita> findByPacienteId(Integer pacienteId);

    // Listar todas las citas de un médico
    List<Cita> findByMedicoId(Integer medicoId);

    // Listar todas las citas de una clínica
    List<Cita> findByClinicaId(Integer clinicaId);

    /**
     * Busca la primera cita en la misma clínica y fechaHora, cuyo estado NO sea CANCELADA.
     * Esto nos sirve para validar solapamiento. 
     * Si ya existe una cita en esa hora (RESERVADA O REALIZADA), retorna Optional con ese registro.
     */
    Optional<Cita> findFirstByClinicaIdAndFechaHoraAndEstadoNot(
        Integer clinicaId,
        LocalDateTime fechaHora,
        Cita.Estado estado
    );
}
