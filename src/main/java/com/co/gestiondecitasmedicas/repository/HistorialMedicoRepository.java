// src/main/java/com/co/gestiondecitasmedicas/repository/HistorialMedicoRepository.java
package com.co.gestiondecitasmedicas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.co.gestiondecitasmedicas.models.HistorialMedico;

public interface HistorialMedicoRepository extends JpaRepository<HistorialMedico, Integer> {

    // Encontrar historial por cita
    HistorialMedico findByCitaId(Integer citaId);

    // Listar todos los historiales de un paciente (vía JPQL)
    @Query("SELECT h FROM HistorialMedico h WHERE h.cita.paciente.id = :pacienteId")
    List<HistorialMedico> findAllByPacienteId(@Param("pacienteId") Integer pacienteId);

    // Listar todos los historiales asociados a las citas de un médico
    @Query("SELECT h FROM HistorialMedico h WHERE h.cita.medico.id = :medicoId")
    List<HistorialMedico> findAllByMedicoId(@Param("medicoId") Integer medicoId);
}
