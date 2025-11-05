// src/main/java/com/example/gestioncitas/controller/AuthController.java
package com.co.gestiondecitasmedicas.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.co.gestiondecitasmedicas.dto.UsuarioDto;
import com.co.gestiondecitasmedicas.models.Rol;
import com.co.gestiondecitasmedicas.service.RolService;
import com.co.gestiondecitasmedicas.service.UsuarioService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RolService rolService;

    /**
     * Página de inicio (landing page) con botones "Iniciar sesión" y "Crear cuenta"
     */
    @GetMapping({"/", "/index"})
    public String index() {
        return "index"; // templates/index.html
    }

    
     // Mostrar formulario de login
     
    @GetMapping("/login")
    public String mostrarLogin(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("errorMsg", "Usuario o contraseña incorrectos");
        }
        return "login"; // templates/login.html
    }

  
     /* formulario de registro
     */
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        // Creamos un DTO vacío
        model.addAttribute("usuarioDto", new UsuarioDto());

        // Cargar lista de roles desde la BD para el SELECT en Thymeleaf
        List<Rol> roles = rolService.listarRoles();
        model.addAttribute("rolesDisponibles", roles);

        return "registro"; // templates/registro.html
    }
    @PostMapping("/registro")
    public String procesarRegistro(
            @ModelAttribute("usuarioDto") UsuarioDto usuarioDto,
            RedirectAttributes redirectAttrs,
            Model model
    ) {
        try {
            usuarioService.registrarOActualizarRoles(usuarioDto);
            redirectAttrs.addFlashAttribute("successMsg",
                "¡Registro exitoso! (o roles agregados). Por favor inicia sesión.");
            return "redirect:/login";
        } catch (RuntimeException ex) {
            model.addAttribute("errorMsg", ex.getMessage());
            model.addAttribute("rolesDisponibles", rolService.listarRoles());
            return "registro";
        }
    }

    /**
     * Cerrar sesión (invalidate)
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
