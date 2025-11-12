// src/main/java/com/co/gestiondecitasmedicas/service/CitaService.java
package com.co.gestiondecitasmedicas.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.co.gestiondecitasmedicas.models.Cita;
import com.co.gestiondecitasmedicas.models.Usuario;

public interface CitaService {

    /**
     * Reserva una nueva cita:
     * - Valida que en esa clínica y fechaHora no exista otra cita (que no esté CANCELADA).
     * - Si está libre, crea la cita con estado=RESERVADA.
     */
    Cita reservarCita(Usuario paciente, Usuario medico, LocalDateTime fechaHora, String correo, String motivo);

    /**
     * Cancela una cita (solo si está en estado RESERVADA) y quienCancela es paciente o médico asociado.
     */
    Cita cancelarCita(Integer citaId, Usuario quienCancela);

    /**
     * Modifica la fecha de una cita (si está RESERVADA, y en la nueva fecha/hora no hay otra cita en esa clínica).
     */
    Cita modificarFechaCita(Integer citaId, LocalDateTime nuevaFechaHora);

    /**
     * Marca una cita como REALIZADA y crea un HistorialMedico con diagnóstico y receta.
     */
    Cita realizarCita(Integer citaId, String diagnostico, String receta);

    /**
     * Listar todas las citas de un paciente.
     */
    List<Cita> listarCitasPorPaciente(Integer pacienteId);

    /**
     * Listar todas las citas de un médico.
     */
    List<Cita> listarCitasPorMedico(Integer medicoId);

    /**
     * Listar todas las citas de una clínica.
     */
    List<Cita> listarCitasPorClinica(Integer clinicaId);

    /**
     * Obtener una cita por su id.
     */
    Cita findById(Integer citaId);

    /**
     * Eliminar (borrar) una cita de una clínica:
     * - Verifica que la cita pertenezca a la clínica dada.
     * - Si coincide, la borra completamente de la base de datos.
     */
    void eliminarCitaDeClinica(Integer citaId, Integer clinicaId);
    
    Cita facturarCita(Integer citaId, BigDecimal valorPagar);
    

}
