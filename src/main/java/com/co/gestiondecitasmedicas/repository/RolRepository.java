// src/main/java/com/example/gestioncitas/repository/RolRepository.java
package com.co.gestiondecitasmedicas.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.co.gestiondecitasmedicas.models.Rol;

public interface RolRepository extends JpaRepository<Rol, Integer> {
    Optional<Rol> findByNombre(String nombre);
}
