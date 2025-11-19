// Clase que implementa la lógica del servicio de historial médico
package com.co.gestiondecitasmedicas.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.co.gestiondecitasmedicas.models.HistorialMedico;
import com.co.gestiondecitasmedicas.repository.HistorialMedicoRepository;

@Service // Indica que esta clase es un servicio de Spring
public class HistorialMedicoServiceImpl implements HistorialMedicoService {

    @Autowired // Inyecta automáticamente el repositorio del historial médico
    private HistorialMedicoRepository historialRepository;

    @Override
    public List<HistorialMedico> listarHistorialesPorPaciente(Integer pacienteId) {
        // Retorna todos los historiales asociados al ID del paciente
        return historialRepository.findAllByPacienteId(pacienteId);
    }

    @Override
    public List<HistorialMedico> listarHistorialesPorMedico(Integer medicoId) {
        // Retorna todos los historiales asociados al ID del médico
        return historialRepository.findAllByMedicoId(medicoId);
    }

    @Override
    public HistorialMedico findByCitaId(Integer citaId) {
        // Busca el historial médico correspondiente a una cita específica
        return historialRepository.findByCitaId(citaId);
    }
}
