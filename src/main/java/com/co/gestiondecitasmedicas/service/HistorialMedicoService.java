// src/main/java/com/co/gestiondecitasmedicas/service/HistorialMedicoService.java
package com.co.gestiondecitasmedicas.service;

import java.util.List;
import com.co.gestiondecitasmedicas.models.HistorialMedico;

public interface HistorialMedicoService {

    List<HistorialMedico> listarHistorialesPorPaciente(Integer pacienteId);
    List<HistorialMedico> listarHistorialesPorMedico(Integer medicoId);
    
    HistorialMedico findByCitaId(Integer citaId);
}
