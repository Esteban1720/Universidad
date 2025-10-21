package com.co.gestiondecitasmedicas.service;

import java.util.List;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.co.gestiondecitasmedicas.dto.UsuarioDto;
import com.co.gestiondecitasmedicas.mapper.UsuarioMapper;
import com.co.gestiondecitasmedicas.models.Clinica;
import com.co.gestiondecitasmedicas.models.Rol;
import com.co.gestiondecitasmedicas.models.Usuario;
import com.co.gestiondecitasmedicas.repository.ClinicaRepository;
import com.co.gestiondecitasmedicas.repository.RolRepository;
import com.co.gestiondecitasmedicas.repository.UsuarioRepository;




import com.co.gestiondecitasmedicas.models.Cita;

import com.co.gestiondecitasmedicas.repository.ClinicaRepository;
import com.co.gestiondecitasmedicas.repository.UsuarioRepository;
import com.co.gestiondecitasmedicas.service.CitaService;
import com.co.gestiondecitasmedicas.service.UsuarioService;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private ClinicaRepository clinicaRepository;

    @Autowired
    private UsuarioMapper usuarioMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    
    // <<< INYECCIÓN QUE FALTABA >>>
    @Autowired
    private CitaService citaService;
    /**
     * Crea un nuevo usuario o agrega roles faltantes, y si incluye el rol CLINICA,
     * crea la entidad Clinica asociada.
     */
    @Override
    @Transactional
    public Usuario registrarOActualizarRoles(UsuarioDto dto) {
        Optional<Usuario> porLogin = usuarioRepository.findByUsuariologin(dto.getUsuariologin());
        Optional<Usuario> porEmail = usuarioRepository.findByEmail(dto.getEmail());

        if (porLogin.isPresent() && !porLogin.get().getEmail().equals(dto.getEmail())) {
            throw new RuntimeException("El nombre de usuario ya está en uso con otro email.");
        }
        if (porEmail.isPresent() && !porEmail.get().getUsuariologin().equals(dto.getUsuariologin())) {
            throw new RuntimeException("El email ya está registrado con otro usuario.");
        }

        Usuario usuario = porLogin.or(() -> porEmail).orElse(null);

        if (usuario == null) {
            String passEnc = passwordEncoder.encode(dto.getPassword());
            dto.setPassword(passEnc);
            usuario = usuarioMapper.toUsuario(dto);
        }

        if (dto.getRolesIds() != null) {
            for (Integer rolId : dto.getRolesIds()) {
                Rol rol = rolRepository.findById(rolId)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + rolId));
                if (!usuario.getRoles().contains(rol)) {
                    usuario.getRoles().add(rol);
                }
            }
        }

        usuario = usuarioRepository.save(usuario);

        // Si el DTO incluye el rol CLINICA, creamos la fila en clinicas
        Optional<Rol> rolClinicaOpt = rolRepository.findByNombre("CLINICA");
        if (rolClinicaOpt.isPresent() && dto.getRolesIds() != null
            && dto.getRolesIds().contains(rolClinicaOpt.get().getId())) {
            boolean existe = clinicaRepository.findByUsuarioId(usuario.getId()).isPresent();
            if (!existe) {
                Clinica clinica = new Clinica();
                clinica.setUsuario(usuario);
                clinica.setNombre(usuario.getNombre());
                clinicaRepository.save(clinica);
            }
        }

        return usuario;
    }

    @Override
    public Optional<Usuario> autenticar(String usuariologin, String passwordPlano) {
        return usuarioRepository.findByUsuariologin(usuariologin)
            .filter(u -> passwordEncoder.matches(passwordPlano, u.getPassword()));
    }

    @Override
    public Optional<Usuario> buscarPorLogin(String usuariologin) {
        return usuarioRepository.findByUsuariologin(usuariologin);
    }

    /**
     * Registra un nuevo médico y lo asocia a la clínica indicada.
     */
    @Override
    @Transactional
    public Usuario registrarMedicoParaClinica(UsuarioDto dto, Integer idClinica) {
        Rol rolMed = rolRepository.findByNombre("MEDICO")
            .orElseThrow(() -> new RuntimeException("Rol MEDICO no existe"));
        dto.setRolesIds(Set.of(rolMed.getId()));

        Usuario medico = registrarOActualizarRoles(dto);

        Clinica clinica = clinicaRepository.findByUsuarioId(idClinica)
            .orElseThrow(() -> new RuntimeException("No se encontró la clínica"));
        medico.setClinica(clinica);

        return usuarioRepository.save(medico);
    }
    
 // ===== Nuevos métodos =====

    @Override
    public List<Usuario> listarMedicosDeClinica(Integer clinicaId) {
        // Usamos el método derivado en UsuarioRepository
        return usuarioRepository.findByClinicaIdAndRolesNombre(clinicaId, "MEDICO");
    }

    @Override
    public List<Clinica> listarTodasLasClinicas() {
        return clinicaRepository.findAll();
    }

    @Override
    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id);
    }
    
    @Override
    public Optional<Clinica> buscarClinicaPorId(Integer id) {
        return clinicaRepository.findById(id);
    }
    
    

    
}
