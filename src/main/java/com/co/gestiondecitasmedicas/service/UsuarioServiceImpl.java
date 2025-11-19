package com.co.gestiondecitasmedicas.service;

// Importaciones necesarias para listas, opcionales y conjuntos
import java.util.List;
import java.util.Optional;
import java.util.Set;

// Importaciones de Spring
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Importaciones de clases de la aplicación
import com.co.gestiondecitasmedicas.dto.UsuarioDto;
import com.co.gestiondecitasmedicas.mapper.UsuarioMapper;
import com.co.gestiondecitasmedicas.models.Clinica;
import com.co.gestiondecitasmedicas.models.Rol;
import com.co.gestiondecitasmedicas.models.Usuario;
import com.co.gestiondecitasmedicas.repository.ClinicaRepository;
import com.co.gestiondecitasmedicas.repository.RolRepository;
import com.co.gestiondecitasmedicas.repository.UsuarioRepository;

// Se importan modelos y servicios adicionales
import com.co.gestiondecitasmedicas.models.Cita;
import com.co.gestiondecitasmedicas.service.CitaService;
import com.co.gestiondecitasmedicas.service.UsuarioService;

// Indica que esta clase es un servicio gestionado por Spring
@Service
public class UsuarioServiceImpl implements UsuarioService {

    // Repositorio para manejar la tabla de usuarios
    @Autowired
    private UsuarioRepository usuarioRepository;

    // Repositorio para manejar roles
    @Autowired
    private RolRepository rolRepository;

    // Repositorio para manejar clínicas
    @Autowired
    private ClinicaRepository clinicaRepository;

    // Convierte DTO a entidad Usuario
    @Autowired
    private UsuarioMapper usuarioMapper;

    // Para encriptar contraseñas
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Inyección del servicio de citas que antes faltaba
    @Autowired
    private CitaService citaService;

    /**
     * Registra un nuevo usuario o actualiza roles.
     * Si un usuario tiene rol CLINICA, le crea registro en la tabla "clinica".
     */
    @Override
    @Transactional
    public Usuario registrarOActualizarRoles(UsuarioDto dto) {

        // Busca si ya existe un usuario por login
        Optional<Usuario> porLogin = usuarioRepository.findByUsuariologin(dto.getUsuariologin());

        // Busca si ya existe por email
        Optional<Usuario> porEmail = usuarioRepository.findByEmail(dto.getEmail());

        // Si login ya existe pero el email NO coincide → error
        if (porLogin.isPresent() && !porLogin.get().getEmail().equals(dto.getEmail())) {
            throw new RuntimeException("El nombre de usuario ya está en uso con otro email.");
        }

        // Si email ya existe pero el login NO coincide → error
        if (porEmail.isPresent() && !porEmail.get().getUsuariologin().equals(dto.getUsuariologin())) {
            throw new RuntimeException("El email ya está registrado con otro usuario.");
        }

        // Si no existe usuario, se crea. Si existe, se reutiliza.
        Usuario usuario = porLogin.or(() -> porEmail).orElse(null);

        if (usuario == null) {
            // Encriptar contraseña antes de guardarla
            String passEnc = passwordEncoder.encode(dto.getPassword());
            dto.setPassword(passEnc);

            // Convertir el DTO a entidad Usuario
            usuario = usuarioMapper.toUsuario(dto);
        }

        // Agregar roles del DTO al usuario
        if (dto.getRolesIds() != null) {
            for (Integer rolId : dto.getRolesIds()) {

                // Buscar rol por ID
                Rol rol = rolRepository.findById(rolId)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + rolId));

                // Si el usuario no tiene ese rol, agregarlo
                if (!usuario.getRoles().contains(rol)) {
                    usuario.getRoles().add(rol);
                }
            }
        }

        // Guardar usuario con roles actualizados
        usuario = usuarioRepository.save(usuario);

        // Si usuario tiene rol CLINICA → crear registro en tabla CLINICA
        Optional<Rol> rolClinicaOpt = rolRepository.findByNombre("CLINICA");

        if (rolClinicaOpt.isPresent()
            && dto.getRolesIds() != null
            && dto.getRolesIds().contains(rolClinicaOpt.get().getId())) {

            // Validar si ya existe la clínica asociada
            boolean existe = clinicaRepository.findByUsuarioId(usuario.getId()).isPresent();

            if (!existe) {
                // Crear clínica
                Clinica clinica = new Clinica();
                clinica.setUsuario(usuario);
                clinica.setNombre(usuario.getNombre());

                // Guardar clínica
                clinicaRepository.save(clinica);
            }
        }

        return usuario; // Retorna usuario final
    }

    // Autentica usuario comparando contraseña ingresada con la encriptada
    @Override
    public Optional<Usuario> autenticar(String usuariologin, String passwordPlano) {
        return usuarioRepository.findByUsuariologin(usuariologin)
            .filter(u -> passwordEncoder.matches(passwordPlano, u.getPassword()));
    }

    // Buscar usuario por login
    @Override
    public Optional<Usuario> buscarPorLogin(String usuariologin) {
        return usuarioRepository.findByUsuariologin(usuariologin);
    }

    /**
     * Registra un médico y lo asocia a una clínica existente.
     */
    @Override
    @Transactional
    public Usuario registrarMedicoParaClinica(UsuarioDto dto, Integer idClinica) {
        
        // Obtener rol MEDICO
        Rol rolMed = rolRepository.findByNombre("MEDICO")
            .orElseThrow(() -> new RuntimeException("Rol MEDICO no existe"));

        // Asignar únicamente el rol MEDICO al DTO
        dto.setRolesIds(Set.of(rolMed.getId()));

        // Crear médico usando el método principal
        Usuario medico = registrarOActualizarRoles(dto);

        // Buscar clínica por ID de usuario
        Clinica clinica = clinicaRepository.findByUsuarioId(idClinica)

