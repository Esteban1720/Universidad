package com.co.gestiondecitasmedicas.service;

import java.util.List;
import java.util.Optional;

import com.co.gestiondecitasmedicas.dto.UsuarioDto;
import com.co.gestiondecitasmedicas.models.Clinica;
import com.co.gestiondecitasmedicas.models.Usuario;

public interface UsuarioService {
    /**
     * Crea un nuevo usuario con los roles indicados,
     * o, si ya existe (por email o login), le añade
     * únicamente los roles que no tenga asignados.
     * Lanza RuntimeException en caso de colisión de datos
     * (e.g. login ya existe con otro email).
     */
    Usuario registrarOActualizarRoles(UsuarioDto usuarioDto);

    Optional<Usuario> autenticar(String usuariologin, String passwordPlano);
    Optional<Usuario> buscarPorLogin(String usuariologin);
 // en UsuarioService.java
    Usuario registrarMedicoParaClinica(UsuarioDto dto, Integer idClinica);
    
    
    // NUEVOS MÉTODOS:
    List<Usuario> listarMedicosDeClinica(Integer clinicaId);
    List<Clinica> listarTodasLasClinicas();
    Optional<Usuario> buscarPorId(Integer id);
    Optional<Clinica> buscarClinicaPorId(Integer id);
   
   

}
