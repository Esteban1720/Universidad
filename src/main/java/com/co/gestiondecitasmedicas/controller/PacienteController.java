package com.co.gestiondecitasmedicas.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.co.gestiondecitasmedicas.dto.CitaDto;
import com.co.gestiondecitasmedicas.models.Cita;
import com.co.gestiondecitasmedicas.models.Clinica;
import com.co.gestiondecitasmedicas.models.HistorialMedico;
import com.co.gestiondecitasmedicas.models.Usuario;
import com.co.gestiondecitasmedicas.service.CitaService;
import com.co.gestiondecitasmedicas.service.HistorialMedicoService;
import com.co.gestiondecitasmedicas.service.UsuarioService;

import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

@Controller
@PreAuthorize("hasRole('PACIENTE')")
@RequestMapping("/paciente")
public class PacienteController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private CitaService citaService;

    @Autowired
    private HistorialMedicoService historialService;

    /**
     * Mostrar el formulario para agendar una nueva cita.
     * - Cargamos la lista de clínicas para el dropdown.
     * - Inicialmente no hay médicos cargados (hasta que elija la clínica).
     */
    @GetMapping("/nueva-cita")
    public String formNuevaCita(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam(value = "clinicaId", required = false) Integer clinicaId,
            Model model
    ) {
        // 1) Lista de todas las clínicas
        List<Clinica> clinicas = usuarioService.listarTodasLasClinicas();
        model.addAttribute("clinicas", clinicas);

        // 2) Si ya eligió una clínica, cargamos sus médicos y la entidad Clinica
        if (clinicaId != null) {
            // a) Lista de médicos para esa clínica
            model.addAttribute("medicosDeLaClinica",
                usuarioService.listarMedicosDeClinica(clinicaId));

            // b) Buscar en la lista de clinicas la que coincide con clinicaId
            Clinica clinicaObj = null;
            for (Clinica c : clinicas) {
                if (c.getId().equals(clinicaId)) {
                    clinicaObj = c;
                    break;
                }
            }
            model.addAttribute("clinicaSeleccionadaObjeto", clinicaObj);
        }

        // 3) DTO vacío para el form y el ID seleccionado
        model.addAttribute("citaDto", new CitaDto());
        model.addAttribute("clinicaSeleccionada", clinicaId);
        return "paciente/nueva-cita";
    }

    /**
     * Procesar guardado de la nueva cita.
     */
    @PostMapping("/nueva-cita")
    public String reservarCita(
            @ModelAttribute("citaDto") CitaDto dto,
            @AuthenticationPrincipal UserDetails ud,
            RedirectAttributes ra
    ) {
        Usuario paciente = usuarioService.buscarPorLogin(ud.getUsername())
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado."));

        Usuario medico = usuarioService.buscarPorId(dto.getMedicoId())
            .orElseThrow(() -> new RuntimeException("Médico no encontrado."));
      
        // Parsear fecha
        LocalDateTime fechaHora = LocalDateTime.parse(
            dto.getFechaHora(),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        );
        String correoContacto = dto.getCorreoContacto();
         String motivo = dto.getMotivo();
        
         
        try {
            citaService.reservarCita(paciente, medico,fechaHora,correoContacto,motivo);
            ra.addFlashAttribute("successMsg", "Cita reservada exitosamente.");
            return "redirect:/paciente/mis-citas";
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("errorMsg", ex.getMessage());
            // Si hubo error, redirigimos a /paciente/nueva-cita?clinicaId=... para mantener la clínica seleccionada
            return "redirect:/paciente/nueva-cita?clinicaId=" + dto.getClinicaId();
        }
    }

    /**
     * Ver todas las citas del paciente.
     */
    @GetMapping("/mis-citas")
    public String verMisCitas(
            @AuthenticationPrincipal UserDetails ud,
            Model model
    ) {
        Usuario paciente = usuarioService.buscarPorLogin(ud.getUsername())
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado."));
        List<Cita> citas = citaService.listarCitasPorPaciente(paciente.getId());
        model.addAttribute("citas", citas);
        return "paciente/mis-citas";
    }

    /**
     * Formulario para editar la fecha de una cita concreta (solo si está RESERVADA).
     */
    @GetMapping("/editar-cita/{id}")
    public String formEditarCita(
            @PathVariable("id") Integer citaId,
            @AuthenticationPrincipal UserDetails ud,
            Model model
    ) {
        Cita cita = citaService.findById(citaId);
        Usuario paciente = usuarioService.buscarPorLogin(ud.getUsername())
            .orElseThrow();

        // Validar que el paciente sea dueño de la cita
        if (!cita.getPaciente().getId().equals(paciente.getId())) {
            return "redirect:/paciente/mis-citas";
        }
        if (!cita.getEstado().equals(Cita.Estado.RESERVADA)) {
            model.addAttribute("errorMsg", "Solo las citas en estado RESERVADA pueden editarse.");
            return "redirect:/paciente/mis-citas";
        }

        model.addAttribute("cita", cita);
        return "paciente/editar-cita";
    }

    /**
     * Procesar la edición de fecha de la cita.
     */
    @PostMapping("/editar-cita/{id}")
    public String editarCita(
            @PathVariable("id") Integer citaId,
            @RequestParam("fechaHora") String nuevaFechaHoraStr,
            @AuthenticationPrincipal UserDetails ud,
            RedirectAttributes ra
    ) {
        Usuario paciente = usuarioService.buscarPorLogin(ud.getUsername())
            .orElseThrow();
        Cita cita = citaService.findById(citaId);

        if (!cita.getPaciente().getId().equals(paciente.getId())) {
            ra.addFlashAttribute("errorMsg", "No tienes permiso para editar esta cita.");
            return "redirect:/paciente/mis-citas";
        }

        LocalDateTime nuevaFecha = LocalDateTime.parse(
            nuevaFechaHoraStr,
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        );

        try {
            citaService.modificarFechaCita(citaId, nuevaFecha);
            ra.addFlashAttribute("successMsg", "Fecha de cita actualizada correctamente.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("errorMsg", ex.getMessage());
        }
        return "redirect:/paciente/mis-citas";
    }

    /**
     * Cancelar la cita (solo si está RESERVADA).
     */
    @PostMapping("/cancelar-cita/{id}")
    public String cancelarCita(
            @PathVariable("id") Integer citaId,
            @AuthenticationPrincipal UserDetails ud,
            RedirectAttributes ra
    ) {
        Usuario paciente = usuarioService.buscarPorLogin(ud.getUsername())
            .orElseThrow();
        try {
            citaService.cancelarCita(citaId, paciente);
            ra.addFlashAttribute("successMsg", "Cita cancelada exitosamente.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("errorMsg", ex.getMessage());
        }
        return "redirect:/paciente/mis-citas";
    }

    
    
    
    
 // al final de la clase PacienteController, antes de la última llave
    /**
     * Ver historial médico de una cita del paciente (solo si está REALIZADA).
     */
    @GetMapping("/citas/historial/{id}")
    public String verHistorialCitaPaciente(
        @PathVariable("id") Integer citaId,
        @AuthenticationPrincipal UserDetails ud,
        Model model,
        RedirectAttributes ra
    ) {
        Usuario paciente = usuarioService.buscarPorLogin(ud.getUsername())
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado."));
        Cita cita = citaService.findById(citaId);

        // Validar que sea su propia cita
        if (!cita.getPaciente().getId().equals(paciente.getId())) {
            ra.addFlashAttribute("errorMsg", "No tienes permiso para ver este historial.");
            return "redirect:/paciente/mis-citas";
        }
        // Validar que esté REALIZADA y tenga historial
        if (cita.getEstado() != Cita.Estado.REALIZADA || cita.getHistorial() == null) {
            ra.addFlashAttribute("errorMsg", "El historial aún no está disponible.");
            return "redirect:/paciente/mis-citas";
        }

        model.addAttribute("cita", cita);
        model.addAttribute("historial", cita.getHistorial());
        return "paciente/historial-cita";
    }
    
    
    
    
    

    @GetMapping("/historial/pdf/{id}")
    public void historialPdfPaciente(
        @PathVariable("id") Integer citaId,
        HttpServletResponse response
    ) throws Exception {
        var historial = historialService.findByCitaId(citaId);
        var cita      = historial.getCita();

        ClassPathResource jasperFile = new ClassPathResource("informes/HistorialMedico.jasper");
        JasperReport report = (JasperReport) JRLoader.loadObject(jasperFile.getInputStream());

        Map<String,Object> row = new HashMap<>();
        row.put("pacienteNombre", cita.getPacienteNombre());
        row.put("medicoNombre",   cita.getMedicoNombre());
        row.put("clinicaNombre",  cita.getClinicaNombre());
        row.put("fechaCita",      java.sql.Timestamp.valueOf(cita.getFechaHora()));
        row.put("fechaImpresion", java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
        row.put("diagnostico",    historial.getDiagnostico());
        row.put("receta",         historial.getReceta());

        JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(List.of(row));
        JasperPrint jp = JasperFillManager.fillReport(report, new HashMap<>(), ds);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
            "inline; filename=historial_cita_" + citaId + ".pdf");
        JasperExportManager.exportReportToPdfStream(jp, response.getOutputStream());
    }
 
    
    
    
    
    
    
    
    
    
    
    

}
