// src/main/java/com/co/gestiondecitasmedicas/service/impl/CitaServiceImpl.java
package com.co.gestiondecitasmedicas.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.co.gestiondecitasmedicas.models.Cita;
import com.co.gestiondecitasmedicas.models.Clinica;
import com.co.gestiondecitasmedicas.models.HistorialMedico;
import com.co.gestiondecitasmedicas.models.Usuario;
import com.co.gestiondecitasmedicas.repository.CitaRepository;
import com.co.gestiondecitasmedicas.repository.ClinicaRepository;
import com.co.gestiondecitasmedicas.repository.HistorialMedicoRepository;

@Service
public class CitaServiceImpl implements CitaService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private HistorialMedicoRepository historialRepository;

    @Autowired
    private ClinicaRepository clinicaRepository;

    @Override
    @Transactional
    public Cita reservarCita(Usuario paciente, Usuario medico,LocalDateTime fechaHora,String correo, String motivo) {
        Integer clinicaId = medico.getClinica().getId();
        Clinica clinica = medico.getClinica();
        Optional<Cita> existe = citaRepository.findFirstByClinicaIdAndFechaHoraAndEstadoNot(
            clinicaId, fechaHora, Cita.Estado.CANCELADA);

        if (existe.isPresent()) {
            throw new RuntimeException("Ya existe otra cita en la misma fecha/hora para esta clínica.");
        }

        Cita cita = new Cita();
        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setClinica(medico.getClinica());
        cita.setFechaHora(fechaHora);
     // ← Asignar los nombres para denormalizar
        cita.setPacienteNombre(paciente.getNombre());
        cita.setMedicoNombre(medico.getNombre());
        cita.setClinicaNombre(clinica.getNombre());
        cita.setDocumento(paciente.getDocumento());
        cita.setCorreoContacto(correo);
        cita.setMotivo(motivo);
        cita.setEstado(Cita.Estado.RESERVADA);

        return citaRepository.save(cita);
    }

    @Override
    @Transactional
    public Cita cancelarCita(Integer citaId, Usuario quienCancela) {
        Cita cita = citaRepository.findById(citaId)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada: " + citaId));

        if (!cita.getEstado().equals(Cita.Estado.RESERVADA)) {
            throw new RuntimeException("Solo se puede cancelar una cita en estado RESERVADA.");
        }

        boolean esPaciente = cita.getPaciente().getId().equals(quienCancela.getId());
        boolean esMedico   = cita.getMedico().getId().equals(quienCancela.getId());
        if (!esPaciente && !esMedico) {
            throw new RuntimeException("No tienes permiso para cancelar esta cita.");
        }

        cita.setEstado(Cita.Estado.CANCELADA);
        return citaRepository.save(cita);
    }

    @Override
    @Transactional
    public Cita modificarFechaCita(Integer citaId, LocalDateTime nuevaFechaHora) {
        Cita cita = citaRepository.findById(citaId)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada: " + citaId));

        if (!cita.getEstado().equals(Cita.Estado.RESERVADA)) {
            throw new RuntimeException("Solo se puede modificar la fecha de una cita que esté en estado RESERVADA.");
        }

        Integer clinicaId = cita.getClinica().getId();
        Optional<Cita> conflicto = citaRepository.findFirstByClinicaIdAndFechaHoraAndEstadoNot(
            clinicaId, nuevaFechaHora, Cita.Estado.CANCELADA);

        if (conflicto.isPresent() && !conflicto.get().getId().equals(citaId)) {
            throw new RuntimeException("Ya existe otra cita en la misma fecha/hora para esta clínica.");
        }

        cita.setFechaHora(nuevaFechaHora);
        return citaRepository.save(cita);
    }
    @Override
    @Transactional
    public Cita realizarCita(Integer citaId, String diagnostico, String receta) {
        Cita cita = citaRepository.findById(citaId)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada: " + citaId));

        if (!cita.getEstado().equals(Cita.Estado.FACTURADA)) {
            throw new RuntimeException(
              "Solo se puede marcar como REALIZADA una cita que esté en estado FACTURADA."
            );
        }

        HistorialMedico historial = new HistorialMedico();
        historial.setDiagnostico(diagnostico);
        historial.setReceta(receta);
        historial.setCita(cita);            // <-- Muy importante
        // Nota: fechaRealizacion ya se inicializa con LocalDateTime.now()

        cita.setHistorial(historial);
        cita.setEstado(Cita.Estado.REALIZADA);
        return citaRepository.save(cita);
    }



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

    @Override
    public Cita findById(Integer citaId) {
        return citaRepository.findById(citaId)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada: " + citaId));
    }

    @Override
    @Transactional
    public void eliminarCitaDeClinica(Integer citaId, Integer clinicaId) {
        Cita cita = citaRepository.findById(citaId)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada: " + citaId));

        if (!cita.getClinica().getId().equals(clinicaId)) {
            throw new RuntimeException("No puedes eliminar una cita que no pertenece a tu clínica.");
        }
        citaRepository.delete(cita);
    }
    
    @Override
    @Transactional
    public Cita facturarCita(Integer citaId, BigDecimal valorPagar) {
        Cita cita = citaRepository.findById(citaId)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada: " + citaId));
        if (cita.getEstado() != Cita.Estado.RESERVADA) {
            throw new RuntimeException("Solo se puede facturar una cita RESERVADA.");
        }
        cita.setValorPagar(valorPagar);
        cita.setEstado(Cita.Estado.FACTURADA);
        return citaRepository.save(cita);
    }
    
    
    
    
    

}
