package com.co.gestiondecitasmedicas.service;

import java.util.List;
import java.util.Optional;

import com.co.gestiondecitasmedicas.dto.UsuarioDto;
import com.co.gestiondecitasmedicas.models.Clinica;
import com.co.gestiondecitasmedicas.models.Usuario;

public interface UsuarioService {

    /**
     * Registra un nuevo usuario o actualiza los roles si ya existe.
     * Si el usuario ya existe (por login o email), se validan colisiones
     * y solo se agregan los roles que aún no tenga asignados.
     *
     * @param usuarioDto Datos del usuario a registrar o actualizar.
     * @return Usuario creado o actualizado.
     */
    Usuario registrarOActualizarRoles(UsuarioDto usuarioDto);

    /**
     * Autentica un usuario comparando el login y la contraseña en texto plano.
     *
     * @param usuariologin Login del usuario.
     * @param passwordPlano Contraseña sin encriptar.
     * @return Usuario autenticado si coincide login/password.
     */
    Optional<Usuario> autenticar(String usuariologin, String passwordPlano);

    /**
     * Busca un usuario por su login exacto.
     *
     * @param usuariologin Login del usuario.
     * @return Optional con el usuario si existe.
     */
    Optional<Usuario> buscarPorLogin(String usuariologin);

    /**
     * Registra un médico asignado directamente a una clínica.
     *
     * @param dto Datos del médico.
     * @param idClinica ID de la clínica a la que pertenece.
     * @return Médico registrado con rol MEDICO y asociado a la clínica.
     */
    Usuario registrarMedicoParaClinica(UsuarioDto dto, Integer idClinica);

    /**
     * Lista todos los médicos asociados a una clínica.
     *
     * @param clinicaId ID de la clínica.
     * @return Lista de médicos de esa clínica.
     */
    List<Usuario> listarMedicosDeClinica(Integer clinicaId);

    /**
     * Lista todas las clínicas del sistema.
     *
     * @return Lista de clínicas.
     */
    List<Clinica> listarTodasLasClinicas();

    /**
     * Busca un usuario por su ID.
     *
     * @param id ID del usuario.
     * @return Optional con el usuario si existe.
     */
    Optional<Usuario> buscarPorId(Integer id);

    /**
     * Busca una clínica por su ID.
     *
     * @param id ID de la clínica.
     * @return Optional con la clínica si existe.
     */
    Optional<Clinica> buscarClinicaPorId(Integer id);
}
