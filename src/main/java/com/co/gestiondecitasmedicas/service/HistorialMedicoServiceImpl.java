// src/main/java/com/co/gestiondecitasmedicas/service/impl/HistorialMedicoServiceImpl.java
package com.co.gestiondecitasmedicas.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.co.gestiondecitasmedicas.models.HistorialMedico;
import com.co.gestiondecitasmedicas.repository.HistorialMedicoRepository;

@Service
public class HistorialMedicoServiceImpl implements HistorialMedicoService {

    @Autowired
    private HistorialMedicoRepository historialRepository;

    @Override
    public List<HistorialMedico> listarHistorialesPorPaciente(Integer pacienteId) {
        return historialRepository.findAllByPacienteId(pacienteId);
    }

    @Override
    public List<HistorialMedico> listarHistorialesPorMedico(Integer medicoId) {
        return historialRepository.findAllByMedicoId(medicoId);
    }

    @Override
    public HistorialMedico findByCitaId(Integer citaId) {
        return historialRepository.findByCitaId(citaId);
    }
}
