package com.co.gestiondecitasmedicas.controller;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.co.gestiondecitasmedicas.models.Usuario;
import com.co.gestiondecitasmedicas.service.UsuarioService;

@Controller
public class HomeController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Al entrar en /home revisamos cuántos roles tiene el usuario.
     * - Si tiene 1, redirige directo al dashboard de ese rol.
     * - Si tiene >1, muestra la vista de selección de rol.
     */
    @GetMapping("/home")
    public String home(
        @AuthenticationPrincipal UserDetails userDetails,
        Model model
    ) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        String login = userDetails.getUsername();
        Optional<Usuario> opt = usuarioService.buscarPorLogin(login);
        if (opt.isEmpty()) {
            return "redirect:/login";
        }
        Usuario usuario = opt.get();
        Set<String> roles = usuario.getRoles()
                                   .stream()
                                   .map(r -> r.getNombre())
                                   .collect(java.util.stream.Collectors.toSet());

        if (roles.size() == 1) {
            // Sólo un rol: redirigimos sin mostrar página intermedia
            String rol = roles.iterator().next();
            return "redirect:" + urlPorRol(rol);
        }

        // Múltiples roles: mostramos selector
        model.addAttribute("nombreUsuario", usuario.getNombre());
        model.addAttribute("roles", roles);
        return "seleccionar-rol";
    }

    /**
     * Recibe el rol seleccionado y redirige al dashboard correspondiente.
     */
    @PostMapping("/home/selectRole")
    public String seleccionarRol(
        @RequestParam("rol") String rol
    ) {
        return "redirect:" + urlPorRol(rol);
    }

    /**
     * Mapea el nombre de rol a la URL de su dashboard.
     */
    private String urlPorRol(String rol) {
        return switch (rol) {
            case "PACIENTE" -> "/paciente/dashboard";
            case "MEDICO"   -> "/medico/dashboard";
            case "CLINICA"  -> "/clinica/dashboard";
            default         -> "/home";  // fallback
        };
    }
    
    
 // src/main/java/com/co/gestiondecitasmedicas/controller/HomeController.java
    @GetMapping("/paciente/dashboard")
    @PreAuthorize("hasRole('PACIENTE')")
    public String dashPaciente(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        model.addAttribute("nombreUsuario", userDetails.getUsername());
        return "paciente/dashboard";
    }

}
