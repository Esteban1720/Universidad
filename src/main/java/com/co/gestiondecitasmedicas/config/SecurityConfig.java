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
