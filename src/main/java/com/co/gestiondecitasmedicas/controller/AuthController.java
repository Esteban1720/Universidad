// src/main/java/com/example/gestioncitas/controller/AuthController.java
/**
 * Esta clase se encarga de manejar todo lo relacionado con el inicio de sesión, 
 * registro y cierre de sesión de los usuarios dentro del sistema de gestión de citas médicas.
 * 
 * En pocas palabras, controla cómo los usuarios entran, se registran y salen del sistema.
 * 
 * Funciones principales:
 * 
 * 1. Página de inicio ("/" o "/index"):
 *    - Muestra la página principal donde el usuario puede elegir entre iniciar sesión o crear una cuenta nueva.
 * 
 * 2. Inicio de sesión ("/login"):
 *    - Muestra el formulario donde el usuario escribe su nombre de usuario y contraseña.
 *    - Si los datos son incorrectos, muestra un mensaje de error en pantalla.
 * 
 * 3. Registro de usuario ("/registro"):
 *    - Muestra un formulario para crear una nueva cuenta.
 *    - Carga la lista de roles disponibles (por ejemplo, “Paciente”, “Médico”, “Administrador”)
 *      desde la base de datos para que el usuario seleccione su rol.
 *    - Cuando el usuario envía el formulario, guarda la información en la base de datos.
 *    - Si todo sale bien, muestra un mensaje de éxito e invita al usuario a iniciar sesión.
 *    - Si ocurre algún error (por ejemplo, un usuario ya registrado), muestra un mensaje explicando el problema.
 * 
 * 4. Cierre de sesión ("/logout"):
 *    - Elimina la sesión actual del usuario (es decir, lo desconecta del sistema) 
 *      y lo redirige a la página de inicio de sesión.
 * 
 * En resumen, este controlador sirve como puente entre las páginas del sitio web 
 * (hechas con Thymeleaf) y la lógica interna del sistema (manejada por los servicios de usuario y rol),
 * garantizando que las acciones básicas de autenticación funcionen correctamente.
 */

















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
