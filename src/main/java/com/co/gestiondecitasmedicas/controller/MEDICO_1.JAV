// src/main/java/com/co/gestiondecitasmedicas/controller/MedicoController.java
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

import com.co.gestiondecitasmedicas.dto.HistorialDto;
import com.co.gestiondecitasmedicas.models.Cita;
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
@PreAuthorize("hasRole('MEDICO')")
@RequestMapping("/medico")
public class MedicoController {

    @Autowired private UsuarioService usuarioService;
    @Autowired private CitaService citaService;
    @Autowired private HistorialMedicoService historialService;

    @GetMapping("/dashboard")
    public String dashMedico(
        @AuthenticationPrincipal UserDetails userDetails,
        Model model
    ) {
        model.addAttribute("nombreUsuario", userDetails.getUsername());
        return "medico/dashboard";
    }

    /**
     * Ver las citas asignadas al médico (todas las que existan, sin importar estado).
     */
    @GetMapping("/citas")
    public String verCitasAsignadas(
            @AuthenticationPrincipal UserDetails ud,
            Model model
    ) {
        Usuario medico = usuarioService.buscarPorLogin(ud.getUsername())
            .orElseThrow(() -> new RuntimeException("Médico no encontrado."));
        List<Cita> citas = citaService.listarCitasPorMedico(medico.getId());
        model.addAttribute("citas", citas);
        return "medico/mis-citas";
    }

    /**
     * Form para editar fecha de cita (solo si está RESERVADA).
     */
    @GetMapping("/editar-cita/{id}")
    public String formEditarFechaCita(
            @PathVariable("id") Integer citaId,
            @AuthenticationPrincipal UserDetails ud,
            Model model
    ) {
        Cita cita = citaService.findById(citaId);
        Usuario medico = usuarioService.buscarPorLogin(ud.getUsername())
            .orElseThrow();

        if (!cita.getMedico().getId().equals(medico.getId())) {
            return "redirect:/medico/citas";
        }
        if (!cita.getEstado().equals(Cita.Estado.RESERVADA)) {
            // Mensaje de error opcional en flash attributes
            return "redirect:/medico/citas";
        }

        model.addAttribute("cita", cita);
        return "medico/editar-cita";
    }

    /**
     * Procesar edición de fecha de cita por parte del médico.
     */
    @PostMapping("/editar-cita/{id}")
    public String editarFechaCita(
            @PathVariable("id") Integer citaId,
            @RequestParam("fechaHora") String nuevaFechaHoraStr,
            @AuthenticationPrincipal UserDetails ud,
            RedirectAttributes ra
    ) {
        Cita cita = citaService.findById(citaId);
        Usuario medico = usuarioService.buscarPorLogin(ud.getUsername())
            .orElseThrow();

        if (!cita.getMedico().getId().equals(medico.getId())) {
            ra.addFlashAttribute("errorMsg", "No tienes permiso para editar esta cita.");
            return "redirect:/medico/citas";
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
        return "redirect:/medico/citas";
    }

    /**
     * Cancelar cita (solo si está RESERVADA).
     */
    @PostMapping("/cancelar-cita/{id}")
    public String cancelarCitaComoMedico(
            @PathVariable("id") Integer citaId,
            @AuthenticationPrincipal UserDetails ud,
            RedirectAttributes ra
    ) {
        Usuario medico = usuarioService.buscarPorLogin(ud.getUsername())
            .orElseThrow();
        try {
            citaService.cancelarCita(citaId, medico);
            ra.addFlashAttribute("successMsg", "Cita cancelada exitosamente.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("errorMsg", ex.getMessage());
        }
        return "redirect:/medico/citas";
    }

    /**
     * Formulario para “realizar” la cita (atender al paciente):
     * aquí se ingresan diagnóstico y receta.
     * Sólo si la cita ya está FACTURADA.
     */
    @GetMapping("/realizar-cita/{id}")
    public String formRealizarCita(
            @PathVariable("id") Integer citaId,
            @AuthenticationPrincipal UserDetails ud,
            Model model
    ) {
        Cita cita = citaService.findById(citaId);
        Usuario medico = usuarioService.buscarPorLogin(ud.getUsername())
                                       .orElseThrow();

        // Permitir sólo si el médico es el asignado y la cita está en FACTURADA
        if (!cita.getMedico().getId().equals(medico.getId())
            || !cita.getEstado().equals(Cita.Estado.FACTURADA)) {
            return "redirect:/medico/citas";
        }

        model.addAttribute("cita", cita);
        model.addAttribute("historialDto", new HistorialDto());
        return "medico/realizar-cita";
    }

    /**
     * Procesar “realizar cita”: crea historial y cambia estado a REALIZADA.
     * Valida nuevamente que esté FACTURADA.
     */
    @PostMapping("/realizar-cita/{id}")
    public String realizarCita(
            @PathVariable("id") Integer citaId,
            @ModelAttribute("historialDto") HistorialDto dto,
            @AuthenticationPrincipal UserDetails ud,
            RedirectAttributes ra
    ) {
        Cita cita = citaService.findById(citaId);
        Usuario medico = usuarioService.buscarPorLogin(ud.getUsername())
                                       .orElseThrow();

        if (!cita.getMedico().getId().equals(medico.getId())
            || !cita.getEstado().equals(Cita.Estado.FACTURADA)) {
            ra.addFlashAttribute("errorMsg", "No puedes realizar esta cita en su estado actual.");
            return "redirect:/medico/citas";
        }

        try {
            citaService.realizarCita(citaId, dto.getDiagnostico(), dto.getReceta());
            ra.addFlashAttribute("successMsg", "Cita marcada como REALIZADA y historial guardado.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("errorMsg", ex.getMessage());
        }
        return "redirect:/medico/citas";
    }
    
    
    /**
     * Ver historial médico de una cita (solo si ya fue REALIZADA).
     */
    @GetMapping("/citas/historial/{id}")
    public String verHistorialCitaMedico(
        @PathVariable("id") Integer citaId,
        @AuthenticationPrincipal UserDetails ud,
        Model model,
        RedirectAttributes ra
    ) {
        Usuario medico = usuarioService.buscarPorLogin(ud.getUsername())
            .orElseThrow(() -> new RuntimeException("Médico no encontrado."));
        Cita cita = citaService.findById(citaId);

        // Validar que sea su propia cita
        if (!cita.getMedico().getId().equals(medico.getId())) {
            ra.addFlashAttribute("errorMsg", "No tienes permiso para ver este historial.");
            return "redirect:/medico/citas";
        }
        // Validar que esté REALIZADA y tenga historial
        if (cita.getEstado() != Cita.Estado.REALIZADA || cita.getHistorial() == null) {
            ra.addFlashAttribute("errorMsg", "El historial aún no está disponible.");
            return "redirect:/medico/citas";
        }

        model.addAttribute("cita", cita);
        model.addAttribute("historial", cita.getHistorial());
        return "medico/historial-cita";
    }
    
    
    
    
    
 

    @GetMapping("/historial-cita/pdf/{id}")
    public void historialPdfMedico(
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
