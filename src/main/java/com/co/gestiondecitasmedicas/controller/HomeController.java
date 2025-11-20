/**
 * Controlador principal que gestiona la navegación inicial del sistema de
 * gestión de citas médicas después del inicio de sesión.
 *
 * Su responsabilidad es determinar a qué panel debe ir un usuario según sus
 * roles (PACIENTE, MÉDICO o CLÍNICA). Si un usuario tiene un solo rol, es
 * redirigido automáticamente. Si tiene varios roles, se muestra una interfaz
 * para que elija.
 *
 * También incluye el acceso al dashboard exclusivo del paciente.
 */
@Controller
public class HomeController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Endpoint principal del sistema después del login.
     *
     * <p>Flujo general:</p>
     * <ul>
     *     <li>Si el usuario no está autenticado, se redirige al login.</li>
     *     <li>Si el usuario tiene 1 solo rol, se redirige automáticamente a su dashboard.</li>
     *     <li>Si el usuario tiene varios roles, se muestra una vista para elegir uno.</li>
     * </ul>
     *
     * @param userDetails Datos del usuario autenticado proporcionados por Spring Security.
     * @param model Objeto usado para enviar datos a la vista.
     * @return Vista o redirección correspondiente.
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
                .collect(Collectors.toSet());

        // Si tiene solo un rol → redirigir directamente
        if (roles.size() == 1) {
            String rol = roles.iterator().next();
            return "redirect:" + urlPorRol(rol);
        }

        // Si tiene varios roles → mostrar pantalla de selección
        model.addAttribute("nombreUsuario", usuario.getNombre());
        model.addAttribute("roles", roles);

        return "seleccionar-rol";
    }

    /**
     * Procesa la selección de rol hecha por un usuario con múltiples roles.
     *
     * @param rol Rol seleccionado por el usuario desde el formulario.
     * @return Redirección hacia el dashboard correspondiente al rol.
     */
    @PostMapping("/home/selectRole")
    public String seleccionarRol(@RequestParam("rol") String rol) {
        return "redirect:" + urlPorRol(rol);
    }

    /**
     * Asocia un nombre de rol con su URL de dashboard correspondiente.
     *
     * @param rol Nombre del rol (PACIENTE, MEDICO, CLINICA).
     * @return URL del dashboard de dicho rol.
     */
    private String urlPorRol(String rol) {
        return switch (rol) {
            case "PACIENTE" -> "/paciente/dashboard";
            case "MEDICO"   -> "/medico/dashboard";
            case "CLINICA"  -> "/clinica/dashboard";
            default         -> "/home"; // fallback en caso de rol desconocido
        };
    }

    /**
     * Dashboard exclusivo para usuarios con el rol PACIENTE.
     *
     * <p>Está protegido por Spring Security usando @PreAuthorize.</p>
     *
     * @param userDetails Información del usuario autenticado.
     * @param model Modelo para pasar datos a la vista.
     * @return Vista Thymeleaf del dashboard de paciente.
     */
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
