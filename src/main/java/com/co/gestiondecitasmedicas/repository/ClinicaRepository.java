package com.co.gestiondecitasmedicas.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.co.gestiondecitasmedicas.models.Clinica;

public interface ClinicaRepository extends JpaRepository<Clinica, Integer> {
    Optional<Clinica> findByUsuarioId(Integer usuarioId);
}
