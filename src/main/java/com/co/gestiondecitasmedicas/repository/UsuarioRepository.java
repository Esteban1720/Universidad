// src/main/java/com/co/gestiondecitasmedicas/repository/UsuarioRepository.java
package com.co.gestiondecitasmedicas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.co.gestiondecitasmedicas.models.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByUsuariologin(String usuariologin);
    Optional<Usuario> findByEmail(String email);

    /**
     * Consulta derivada: retorna todos los usuarios cuyo campo "clinica.id" = clinicaId, 
     * y que además tengan en su set de roles un Rol con nombre = nombreRol.
     */
    List<Usuario> findByClinicaIdAndRolesNombre(Integer clinicaId, String nombreRol);

    // Método auxiliar para listar todas las clínicas (cada clínica se guarda en la tabla 'clinicas', 
    // pero si queremos listar las clínicas activas, podemos hacer un query sobre ClinicaRepository).
}
