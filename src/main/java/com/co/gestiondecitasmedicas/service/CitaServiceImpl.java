// src/main/java/com/co/gestiondecitasmedicas/service/impl/CitaServiceImpl.java
package com.co.gestiondecitasmedicas.service;

import java.math.BigDecimal;              // Para valores monetarios
import java.time.LocalDateTime;           // Manejo de fechas y horas
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;  // Inyección de dependencias
import org.springframework.stereotype.Service;                 // Marca clase como servicio
import org.springframework.transaction.annotation.Transactional; // Manejo de transacciones

import com.co.gestiondecitasmedicas.models.Cita;
import com.co.gestiondecitasmedicas.models.Clinica;
import com.co.gestiondecitasmedicas.models.HistorialMedico;
import com.co.gestiondecitasmedicas.models.Usuario;
import com.co.gestiondecitasmedicas.repository.CitaRepository;
import com.co.gestiondecitasmedicas.repository.ClinicaRepository;
import com.co.gestiondecitasmedicas.repository.HistorialMedicoRepository;

@Service // Indica que es un servicio manejado por Spring
public class CitaServiceImpl implements CitaService {

    @Autowired // Repositorio para CRUD de citas
    private CitaRepository citaRepository;

    @Autowired // CRUD del historial médico
    private HistorialMedicoRepository historialRepository;

    @Autowired // CRUD de clínicas
    private ClinicaRepository clinicaRepository;

    // ================================================
    //    RESERVAR UNA CITA
    // ================================================
    @Override
    @Transactional
    public Cita reservarCita(Usuario paciente, Usuario medico,LocalDateTime fechaHora,String correo, String motivo) {

        // Obtener el ID de la clínica del médico
        Integer clinicaId = medico.getClinica().getId();
        Clinica clinica = medico.getClinica();

        // Verificar si ya existe una cita en esa fecha/hora para la clínica
        Optional<Cita> existe = citaRepository.findFirstByClinicaIdAndFechaHoraAndEstadoNot(
            clinicaId, fechaHora, Cita.Estado.CANCELADA);

        // Si existe cita y no está cancelada → error
        if (existe.isPresent()) {
            throw new RuntimeException("Ya existe otra cita en la misma fecha/hora para esta clínica.");
        }

        // Crear una nueva cita
        Cita cita = new Cita();
        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setClinica(medico.getClinica());
        cita.setFechaHora(fechaHora);

        // Guardar nombres para denormalización (mejora el rendimiento al listar)
        cita.setPacienteNombre(paciente.getNombre());
        cita.setMedicoNombre(medico.getNombre());
        cita.setClinicaNombre(clinica.getNombre());

        // Datos adicionales
        cita.setDocumento(paciente.getDocumento());
        cita.setCorreoContacto(correo);
        cita.setMotivo(motivo);

        // Estado inicial
        cita.setEstado(Cita.Estado.RESERVADA);

        // Guardar en BD
        return citaRepository.save(cita);
    }

    // ================================================
    //    CANCELAR UNA CITA
    // ================================================
    @Override
    @Transactional
    public Cita cancelarCita(Integer citaId, Usuario quienCancela) {

        // Buscar la cita
        Cita cita = citaRepository.findById(citaId)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada: " + citaId));

        // Solo se cancelan citas RESERVADAS
        if (!cita.getEstado().equals(Cita.Estado.RESERVADA)) {
            throw new RuntimeException("Solo se puede cancelar una cita en estado RESERVADA.");
        }

        // Validar si quien cancela es el paciente o el médico
        boolean esPaciente = cita.getPaciente().getId().equals(quienCancela.getId());
        boolean esMedico   = cita.getMedico().getId().equals(quienCancela.getId());

        if (!esPaciente && !esMedico) {
            throw new RuntimeException("No tienes permiso para cancelar esta cita.");
        }

        // Cambiar estado
        cita.setEstado(Cita.Estado.CANCELADA);

        return citaRepository.save(cita);
    }

    // ================================================
    //   MODIFICAR LA FECHA DE UNA CITA
    // ================================================
    @Override
    @Transactional
    public Cita modificarFechaCita(Integer citaId, LocalDateTime nuevaFechaHora) {

        // Buscar cita
        Cita cita = citaRepository.findById(citaId)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada: " + citaId));

        // Debe estar en estado RESERVADA
        if (!cita.getEstado().equals(Cita.Estado.RESERVADA)) {
            throw new RuntimeException("Solo se puede modificar la fecha de una cita que esté en estado RESERVADA.");
        }

        // Verificar conflicto de horario
        Integer clinicaId = cita.getClinica().getId();
        Optional<Cita> conflicto = citaRepository.findFirstByClinicaIdAndFechaHoraAndEstadoNot(
            clinicaId, nuevaFechaHora, Cita.Estado.CANCELADA);

        // Si hay conflicto y no es la misma cita → error
        if (conflicto.isPresent() && !conflicto.get().getId().equals(citaId)) {
            throw new RuntimeException("Ya existe otra cita en la misma fecha/hora para esta clínica.");
        }

        // Actualizar fecha
        cita.setFechaHora(nuevaFechaHora);
        return citaRepository.save(cita);
    }

    // ================================================
    //    MARCAR COMO REALIZADA Y CREAR HISTORIAL
    // ================================================
    @Override
    @Transactional
    public Cita realizarCita(Integer citaId, String diagnostico, String receta) {

        // Buscar cita
        Cita cita = citaRepository.findById(citaId)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada: " + citaId));

        // Solo se realiza una cita facturada
        if (!cita.getEstado().equals(Cita.Estado.FACTURADA)) {
            throw new RuntimeException(
              "Solo se puede marcar como REALIZADA una cita que esté en estado FACTURADA."
            );
        }

        // Crear historial médico asociado
        HistorialMedico historial = new HistorialMedico();
        historial.setDiagnostico(diagnostico);
        historial.setReceta(receta);
        historial.setCita(cita);  // Relacionar historial con la cita

        // Guardar historial en la entidad cita
        cita.setHistorial(historial);
        cita.setEstado(Cita.Estado.REALIZADA);

        return citaRepository.save(cita);
    }

    // ===============================================
    //   LISTAR CITAS POR PACIENTE / MÉDICO / CLÍNICA
    // ===============================================

    @Override
    public List<Cita> listarCitasPorPaciente(Integer pacienteId) {
        return citaRepository.findByPacienteId(pacienteId);
    }

    @Override
    public List<Cita> listarCitasPorMedico(Integer medicoId) {
        return citaRepository.findByMedicoId(medicoId);
    }

    @Override
    public List<Cita> listarCitasPorClinica(Integer clinicaId) {
        return citaRepository.findByClinicaId(clinicaId);
    }

    // ===============================================
    //         BUSCAR CITA POR ID
    // ===============================================
    @Override
    public Cita findById(Integer citaId) {
        return citaRepository.findById(citaId)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada: " + citaId));
    }

    // ===============================================
    //       ELIMINAR CITA DESDE UNA CLÍNICA
    // ===============================================
    @Override
    @Transactional
    public void eliminarCitaDeClinica(Integer citaId, Integer clinicaId) {

        // Obtener cita
        Cita cita = citaRepository.findById(citaId)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada: " + citaId));

        // Verificar que la cita pertenece a la clínica
        if (!cita.getClinica().getId().equals(clinicaId)) {
            throw new RuntimeException("No puedes eliminar una cita que no pertenece a tu clínica.");
        }

        citaRepository.delete(cita); // Eliminar
    }

    // ===============================================
    //             FACTURAR UNA CITA
    // ===============================================
    @Override
    @Transactional
    public Cita facturarCita(Integer citaId, BigDecimal valorPagar) {

        // Buscar cita
        Cita cita = citaRepository.findById(citaId)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada: " + citaId));

        // Solo citas reservadas pueden ser facturadas
        if (cita.getEstado() != Cita.Estado.RESERVADA) {
            throw new RuntimeException("Solo se puede facturar una cita RESERVADA.");
        }

        cita.setValorPagar(valorPagar);
        cita.setEstado(Cita.Estado.FACTURADA);

        return citaRepository.save(cita);
    }
}
