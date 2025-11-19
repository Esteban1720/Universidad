package com.co.gestiondecitasmedicas.service;

// Importación de listas
import java.util.List;

// Manejo de Optional para valores que pueden no existir
import java.util.Optional;
// Manejo de conjuntos (para roles)
import java.util.Set;

// Importaciones de Spring para inyección de dependencias y transacciones
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Importación de DTO, Mapper, Modelos y Repositorios
import com.co.gestiondecitasmedicas.dto.UsuarioDto;
import com.co.gestiondecitasmedicas.mapper.UsuarioMapper;
import com.co.gestiondecitasmedicas.models.Clinica;
import com.co.gestiondecitasmedicas.models.Rol;
import com.co.gestiondecitasmedicas.models.Usuario;
import com.co.gestiondecitasmedicas.repository.ClinicaRepository;
import com.co.gestiondecitasmedicas.repository.RolRepository;
import com.co.gestiondecitasmedicas.repository.UsuarioRepository;

// Modelo Cita (solo importado)
import com.co.gestiondecitasmedicas.models.Cita;

// Repositorios e interfaces del servicio
import com.co.gestiondecitasmedicas.repository.ClinicaRepository;
import com.co.gestiondecitasmedicas.repository.UsuarioRepository;
import com.co.gestiondecitasmedicas.service.CitaService;
import com.co.gestiondecitasmedicas.service.UsuarioService;

// Marcamos esta clase como un servicio de Spring
@Service
public class UsuarioServiceImpl implements UsuarioService {

    // Inyección del repositorio de usuarios
    @Autowired
    private UsuarioRepository usuarioRepository;

    // Inyección del repositorio de roles
    @Autowired
    private RolRepository rolRepository;

    // Inyección del repositorio de clínicas
    @Autowired
    private ClinicaRepository clinicaRepository;

    // Mapper para convertir entre UsuarioDto y Usuario
    @Autowired
    private UsuarioMapper usuarioMapper;

    // Encriptación de contraseñas
    @Autowired
    private PasswordEncoder passwordEncoder;

    
    // <<< INYECCIÓN QUE FALTABA >>>
    // Servicio para manejar citas
    @Autowired
    private CitaService citaService;

    /**
     * Método para registrar un usuario o actualizar roles existentes.
     * Si incluye el rol CLINICA, crea la entidad Clinica vinculada.
     */
    @Override
    @Transactional
    public Usuario registrarOActualizarRoles(UsuarioDto dto) {

        // Buscar usuario por login
        Optional<Usuario> porLogin = usuarioRepository.findByUsuariologin(dto.getUsuariologin());
        // Buscar usuario por email
        Optional<Usuario> porEmail = usuarioRepository.findByEmail(dto.getEmail());

        // Validación: el login ya está en uso por otro email
        if (porLogin.isPresent() && !porLogin.get().getEmail().equals(dto.getEmail())) {
            throw new RuntimeException("El nombre de usuario ya está en uso con otro email.");
        }

        // Validación: el email ya pertenece a otro login
        if (porEmail.isPresent() && !porEmail.get().getUsuariologin().equals(dto.getUsuariologin())) {
            throw new RuntimeException("El email ya está registrado con otro usuario.");
        }

        // Obtenemos el usuario según login o email, o null si no existe
        Usuario usuario = porLogin.or(() -> porEmail).orElse(null);

        // Si el usuario NO existe, lo creamos nuevo
        if (usuario == null) {
            // Encriptamos la contraseña
            String passEnc = passwordEncoder.encode(dto.getPassword());
            dto.setPassword(passEnc);

            // Convertimos el DTO a entidad Usuario con el mapper
            usuario = usuarioMapper.toUsuario(dto);
        }

        // Agregar roles según IDs recibidos en el DTO
        if (dto.getRolesIds() != null) {
            for (Integer rolId : dto.getRolesIds()) {
                // Buscar rol por ID
                Rol rol = rolRepository.findById(rolId)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + rolId));
                
                // Evitar agregar roles repetidos
                if (!usuario.getRoles().contains(rol)) {
                    usuario.getRoles().add(rol);
                }
            }
        }

        // Guardamos el usuario actualizado o nuevo
        usuario = usuarioRepository.save(usuario);

        // Revisamos si el usuario tiene el rol de CLINICA
        Optional<Rol> rolClinicaOpt = rolRepository.findByNombre("CLINICA");

        if (rolClinicaOpt.isPresent() && dto.getRolesIds() != null
            && dto.getRolesIds().contains(rolClinicaOpt.get().getId())) {

            // Verificar si ya tiene una clínica creada
            boolean existe = clinicaRepository.findByUsuarioId(usuario.getId()).isPresent();
            
            // Si no existe, se crea
            if (!existe) {
                Clinica clinica = new Clinica();
                clinica.setUsuario(usuario);        // Relación con el usuario
                clinica.setNombre(usuario.getNombre()); // Se usa el nombre del usuario
                clinicaRepository.save(clinica);
            }
        }

        return usuario; // Se retorna el usuario registrado o actualizado
    }

    // Autenticación: valida login y contraseña encriptada
    @Override
    public Optional<Usuario> autenticar(String usuariologin, String passwordPlano) {
        return usuarioRepository.findByUsuariologin(usuariologin)
            .filter(u -> passwordEncoder.matches(passwordPlano, u.getPassword()));
    }

    // Busca usuario por login
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

        // Buscar rol MEDICO
        Rol rolMed = rolRepository.findByNombre("MEDICO")
            .orElseThrow(() -> new RuntimeException("Rol MEDICO no existe"));

        // Asignar el rol MEDICO al DTO
        dto.setRolesIds(Set.of(rolMed.getId()));

        // Registrar el usuario como médico
        Usuario medico = registrarOActualizarRoles(dto);

        // Buscar la clínica por id de usuario asociado
        Clinica clinica = clinicaRepository.findByUsuarioId(idClinica)
            .orElseThrow(() -> new RuntimeException("No se encontró la clínica"));

        // Asociar médico a la clínica
        medico.setClinica(clinica);

        return usuarioRepository.save(medico);
    }
    
    // ===== Nuevos métodos =====

    // Lista los médicos de una clínica según el ID
    @Override
    public List<Usuario> listarMedicosDeClinica(Integer clinicaId) {
        return usuarioRepository.findByClinicaIdAndRolesNombre(clinicaId, "MEDICO");
    }

    // Lista todas las clínicas registradas
    @Override
    public List<Clinica> listarTodasLasClinicas() {
        return clinicaRepository.findAll();
    }

    // Buscar usuario por ID
    @Override
    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id);
    }
    
    // Buscar clínica por ID
    @Override
    public Optional<Clinica> buscarClinicaPorId(Integer id) {
        return clinicaRepository.findById(id);
    }
    
} 

