/**
 * Configuración central de seguridad para el sistema de gestión de citas médicas.
 *
 * Esta clase define cómo Spring Security:
 *  - Autentica a los usuarios (login)
 *  - Verifica contraseñas
 *  - Asigna roles
 *  - Controla qué rutas requieren autenticación
 *  - Administra los formularios de inicio/cierre de sesión
 *
 * COMPONENTES PRINCIPALES:
 *
 * 1. UserDetailsService (cargar usuario desde BD)
 *    - Busca un usuario en la base de datos usando su nombre de usuario.
 *    - Convierte sus roles (por ejemplo "ADMIN", "PACIENTE") en permisos
 *      entendibles por Spring Security ("ROLE_ADMIN", "ROLE_PACIENTE").
 *    - Devuelve un objeto UserDetails que Spring Security usa para validar login.
 *
 * 2. PasswordEncoder (BCrypt)
 *    - Se encarga de encriptar y verificar contraseñas.
 *    - Usa BCrypt, que es el algoritmo estándar recomendado por Spring.
 *
 * 3. AuthenticationProvider
 *    - Combina el UserDetailsService y el PasswordEncoder.
 *    - Spring Security lo usa para comparar contraseña ingresada vs contraseña en BD.
 *
 * 4. SecurityFilterChain (configuración HTTP)
 *    - Define qué rutas pueden visitarse sin iniciar sesión (pública).
 *    - Define qué rutas requieren autenticación.
 *    - Configura el formulario de login:
 *        * URL del formulario
 *        * A dónde enviar los datos
 *        * Qué nombres tienen los campos del login
 *        * A dónde redirigir si es exitoso o si falla
 *    - Configura el logout para cerrar sesión correctamente.
 *
 * RUTAS PÚBLICAS:
 *    "/", "/index", "/login", "/registro", "/css/**", "/js/**", "/images/**"
 *
 * RUTAS PROTEGIDAS:
 *    - Todas las demás requieren usuario autenticado.
 *
 * En resumen:
 * Esta clase es el corazón de la seguridad del sistema. Controla todo lo relacionado
 * con autenticación, roles, rutas protegidas, encriptación de contraseñas
 * y manejo general de sesiones en la aplicación.
 */


package com.co.gestiondecitasmedicas.config;

import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.co.gestiondecitasmedicas.models.Rol;
import com.co.gestiondecitasmedicas.models.Usuario;
import com.co.gestiondecitasmedicas.repository.UsuarioRepository;

@Configuration
public class SecurityConfig {

    // 1) Bean para cargar usuarios desde la BD en Spring Security
    @Bean
    public UserDetailsService userDetailsService(UsuarioRepository repo) {
        return username -> {
            Usuario u = repo.findByUsuariologin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

            var authorities = u.getRoles().stream()
                .map(Rol::getNombre)
                .map(r -> "ROLE_" + r)
                .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

            return User.builder()
                .username(u.getUsuariologin())
                .password(u.getPassword())
                .authorities(authorities)
                .build();
        };
    }

    // 2) Bean para codificar y verificar contraseñas con BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 3) Configuramos un proveedor de autenticación con nuestro UserDetailsService
    @Bean
    public AuthenticationProvider authenticationProvider(UsuarioRepository repo) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService(repo));
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // 4) Seguridad HTTP: filtros, rutas, login y logout
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationProvider authProvider) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authenticationProvider(authProvider)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/index", "/login", "/registro", "/css/**", "/js/**", "/images/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("usuariologin")    // coincide con tu campo del formulario
                .passwordParameter("password")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
