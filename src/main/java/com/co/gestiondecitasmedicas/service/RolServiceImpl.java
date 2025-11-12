// src/main/java/com/example/gestioncitas/service/impl/RolServiceImpl.java
package com.co.gestiondecitasmedicas.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.co.gestiondecitasmedicas.models.Rol;
import com.co.gestiondecitasmedicas.repository.RolRepository;

@Service
public class RolServiceImpl implements RolService {

    @Autowired
    private RolRepository rolRepository;

    @Override
    public List<Rol> listarRoles() {
        return rolRepository.findAll();
    }
}
