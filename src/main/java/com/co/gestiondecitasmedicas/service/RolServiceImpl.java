// src/main/java/com/example/gestioncitas/service/impl/RolServiceImpl.java

package com.co.gestiondecitasmedicas.service;

// Importa la clase List para manejar listas de objetos
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Importa el modelo Rol y el repositorio correspondiente
import com.co.gestiondecitasmedicas.models.Rol;
import com.co.gestiondecitasmedicas.repository.RolRepository;

// Marca esta clase como un servicio de Spring, permitiendo inyección de dependencias
@Service
public class RolServiceImpl implements RolService {

    // Inyección del repositorio que permite acceder a la base de datos de roles
    @Autowired
    private RolRepository rolRepository;

    // Implementación del método listarRoles de la interfaz RolService
    @Override
    public List<Rol> listarRoles() {
        // Retorna todos los roles registrados en la base de datos
        return rolRepository.findAll();
    }
}
