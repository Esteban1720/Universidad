// src/main/java/com/example/gestioncitas/mapper/UsuarioMapper.java
package com.co.gestiondecitasmedicas.mapper;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.co.gestiondecitasmedicas.dto.UsuarioDto;
import com.co.gestiondecitasmedicas.models.Rol;
import com.co.gestiondecitasmedicas.models.Usuario;
import com.co.gestiondecitasmedicas.repository.RolRepository;

@Component
public class UsuarioMapper {

    @Autowired
    private RolRepository rolRepository;

    /**
     * Convierte un UsuarioDto a Usuario (entidad). 
     * - Busca todos los roles con los IDs que trae el DTO.
     * - No encripta la contraseña; asume que el caller ya hizo BCrypt o similar.
     */
    public Usuario toUsuario(UsuarioDto dto) {
        Usuario u = new Usuario();
        u.setNombre(dto.getNombre());
        u.setUsuariologin(dto.getUsuariologin());
        u.setDocumento(dto.getDocumento());
        u.setEmail(dto.getEmail());
        u.setTelefono(dto.getTelefono());
        u.setDireccion(dto.getDireccion());
        u.setPassword(dto.getPassword()); // debe estar codificada antes de llamar a este mapper

        // Convertir rolesIds a Set<Rol>
        Set<Rol> roles = new HashSet<>();
        if (dto.getRolesIds() != null) {
            for (Integer rolId : dto.getRolesIds()) {
                Rol rol = rolRepository.findById(rolId)
                        .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + rolId));
                roles.add(rol);
            }
        }
        u.setRoles(roles);
        return u;
    }

    /**
     * Convierte entidad Usuario a UsuarioDto (por si necesitas enviar datos a la vista).
     */
    public UsuarioDto toUsuarioDto(Usuario u) {
        UsuarioDto dto = new UsuarioDto();
        dto.setNombre(u.getNombre());
        dto.setUsuariologin(u.getUsuariologin());
        dto.setDocumento(u.getDocumento());
        dto.setEmail(u.getEmail());
        dto.setTelefono(u.getTelefono());
        dto.setDireccion(u.getDireccion());
        // Nota: no devolvemos la password en el DTO por seguridad.

        Set<Integer> ids = new HashSet<>();
        if (u.getRoles() != null) {
            for (Rol rol : u.getRoles()) {
                ids.add(rol.getId());
            }
        }
        dto.setRolesIds(ids);
        return dto;
    }
}
