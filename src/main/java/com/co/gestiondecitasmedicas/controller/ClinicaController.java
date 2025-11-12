// src/main/java/com/co/gestiondecitasmedicas/controller/ClinicaController.java
package com.co.gestiondecitasmedicas.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.mail.internet.MimeMessage;                        // CORRECTO: usar Jakarta Mail
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.co.gestiondecitasmedicas.dto.UsuarioDto;
import com.co.gestiondecitasmedicas.models.Cita;
import com.co.gestiondecitasmedicas.models.Clinica;
import com.co.gestiondecitasmedicas.models.Usuario;
import com.co.gestiondecitasmedicas.repository.ClinicaRepository;
import com.co.gestiondecitasmedicas.service.CitaService;
import com.co.gestiondecitasmedicas.service.HistorialMedicoService;
import com.co.gestiondecitasmedicas.service.RolService;
import com.co.gestiondecitasmedicas.service.UsuarioService;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

@Controller
@PreAuthorize("hasRole('CLINICA')")
@RequestMapping("/clinica")
public class ClinicaController {

    @Autowired private UsuarioService usuarioService;
    @Autowired private RolService rolService;
    @Autowired private ClinicaRepository clinicaRepository;
    @Autowired private CitaService citaService;
    @Autowired private JavaMailSender mailSender;  // Para enviar correos

    @GetMapping("/dashboard")
    public String dashClinica(@AuthenticationPrincipal UserDetails ud, Model model) {
        model.addAttribute("nombreUsuario", ud.getUsername());
        return "clinica/dashboard";
    }

    @GetMapping("/medicos/nuevo")
    public String formNuevoMedico(Model model) {
        model.addAttribute("usuarioDto", new UsuarioDto());
        return "clinica/nuevo-medico";
    }

    @PostMapping("/medicos/nuevo")
    public String crearMedico(
        @ModelAttribute UsuarioDto dto,
        @AuthenticationPrincipal UserDetails ud,
        RedirectAttributes ra
    ) {
        Usuario cli = usuarioService.buscarPorLogin(ud.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
        usuarioService.registrarMedicoParaClinica(dto, cli.getId());
        ra.addFlashAttribute("successMsg", "Médico registrado correctamente.");
        return "redirect:/clinica/dashboard";
    }

    @GetMapping("/medicos")
    public String listarMedicos(
        @AuthenticationPrincipal UserDetails ud,
        Model model
    ) {
        Usuario usuLog = usuarioService.buscarPorLogin(ud.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
        Clinica clinica = clinicaRepository.findByUsuarioId(usuLog.getId())
            .orElseThrow(() -> new RuntimeException("Clínica no encontrada."));
        List<Usuario> medicos = usuarioService.listarMedicosDeClinica(clinica.getId());
        model.addAttribute("medicos", medicos);
        model.addAttribute("nombreUsuario", usuLog.getNombre());
        return "clinica/ver-medicos";
    }

    @GetMapping("/citas")
    public String verCitasClinica(
        @AuthenticationPrincipal UserDetails ud,
        Model model
    ) {
        Usuario usuLog = usuarioService.buscarPorLogin(ud.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
        Clinica clinica = clinicaRepository.findByUsuarioId(usuLog.getId())
            .orElseThrow(() -> new RuntimeException("Clínica no encontrada."));
        List<Cita> citas = citaService.listarCitasPorClinica(clinica.getId());
        model.addAttribute("citas", citas);
        model.addAttribute("nombreUsuario", usuLog.getNombre());
        return "clinica/ver-citas";
    }

    @PostMapping("/citas/eliminar/{id}")
    public String eliminarCita(
        @PathVariable("id") Integer citaId,
        @AuthenticationPrincipal UserDetails ud
    ) {
        Usuario usuLog = usuarioService.buscarPorLogin(ud.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
        Clinica clinica = clinicaRepository.findByUsuarioId(usuLog.getId())
            .orElseThrow(() -> new RuntimeException("Clínica no encontrada."));
        citaService.eliminarCitaDeClinica(citaId, clinica.getId());
        return "redirect:/clinica/citas";
    }

    @GetMapping("/citas/facturar/{id}")
    public String formFacturarCita(
        @PathVariable("id") Integer citaId,
        @AuthenticationPrincipal UserDetails ud,
        Model model
    ) {
        Usuario usuLog = usuarioService.buscarPorLogin(ud.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
        Clinica clinica = clinicaRepository.findByUsuarioId(usuLog.getId())
            .orElseThrow(() -> new RuntimeException("Clínica no encontrada."));
        Cita cita = citaService.findById(citaId);
        if (!cita.getClinica().getId().equals(clinica.getId())) {
            throw new RuntimeException("No tienes permiso para facturar esta cita.");
        }
        model.addAttribute("cita", cita);
        return "clinica/facturar-cita";
    }

    @PostMapping("/citas/facturar/{id}")
    public String facturarCita(
        @PathVariable("id") Integer citaId,
        @RequestParam("valorPagar") BigDecimal valorPagar,
        @AuthenticationPrincipal UserDetails ud,
        RedirectAttributes ra
    ) {
        Usuario usuLog = usuarioService.buscarPorLogin(ud.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
        Clinica clinica = clinicaRepository.findByUsuarioId(usuLog.getId())
            .orElseThrow(() -> new RuntimeException("Clínica no encontrada."));
        Cita cita = citaService.findById(citaId);

        if (!cita.getClinica().getId().equals(clinica.getId())) {
            ra.addFlashAttribute("errorMsg", "No tienes permiso para facturar esta cita.");
            return "redirect:/clinica/citas";
        }

        try {
            // 1) Actualiza estado y guarda valor
            Cita facturada = citaService.facturarCita(citaId, valorPagar);

            // 2) Genera PDF en memoria
            ClassPathResource jasperFile =
                new ClassPathResource("informes/Facturacion.jasper");
            JasperReport report = (JasperReport)
                JRLoader.loadObject(jasperFile.getInputStream());

            Map<String,Object> row = new HashMap<>();
            row.put("clinicaNombre",  facturada.getClinicaNombre());
            row.put("fechaHora",      java.sql.Timestamp.valueOf(facturada.getFechaHora()));
            row.put("medicoNombre",   facturada.getMedicoNombre());
            row.put("pacienteNombre", facturada.getPacienteNombre());
            row.put("documento",      facturada.getDocumento());
            row.put("motivo",         facturada.getMotivo());
            row.put("valorPagar",     facturada.getValorPagar());

            JRBeanCollectionDataSource ds =
                new JRBeanCollectionDataSource(List.of(row));
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                report, new HashMap<>(), ds
            );

            byte[] pdfBytes;
            try (var baos = new java.io.ByteArrayOutputStream()) {
                JasperExportManager.exportReportToPdfStream(jasperPrint, baos);
                pdfBytes = baos.toByteArray();
            }

            // 3) Envía correo al paciente
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            helper.setTo(facturada.getCorreoContacto());
            helper.setSubject("Factura de su Cita #" + citaId);
            helper.setText(
              "Estimado/a " + facturada.getPacienteNombre() + ",\n\n"
              + "Adjunto encontrarás la factura de su cita.\n\n"
              + "Saludos,\n" + facturada.getClinicaNombre(),
              false
            );
            helper.addAttachment(
              "factura_cita_" + citaId + ".pdf",
              new ByteArrayResource(pdfBytes)
            );
            mailSender.send(mensaje);

            ra.addFlashAttribute("successMsg",
                "Cita facturada y factura enviada al correo de contacto."
            );
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMsg", "Error al facturar o enviar correo: " + ex.getMessage());
        }

        return "redirect:/clinica/citas";
    }

    @GetMapping("/citas/factura/{id}")
    public void verFactura(
        @PathVariable("id") Integer citaId,
        @AuthenticationPrincipal UserDetails ud,
        HttpServletResponse response
    ) throws Exception {
        Cita cita = citaService.findById(citaId);

        ClassPathResource jasperFile =
            new ClassPathResource("informes/Facturacion.jasper");
        JasperReport report = (JasperReport)
            JRLoader.loadObject(jasperFile.getInputStream());

        Map<String,Object> row = new HashMap<>();
        row.put("clinicaNombre",  cita.getClinicaNombre());
        row.put("fechaHora",      java.sql.Timestamp.valueOf(cita.getFechaHora()));
        row.put("medicoNombre",   cita.getMedicoNombre());
        row.put("pacienteNombre", cita.getPacienteNombre());
        row.put("documento",      cita.getDocumento());
        row.put("motivo",         cita.getMotivo());
        row.put("valorPagar",     cita.getValorPagar());

        JRBeanCollectionDataSource ds =
            new JRBeanCollectionDataSource(List.of(row));
        JasperPrint jasperPrint = JasperFillManager.fillReport(report, new HashMap<>(), ds);

        response.setContentType("application/pdf");
        response.setHeader(
          "Content-Disposition",
          "inline; filename=factura_cita_" + citaId + ".pdf"
        );
        JasperExportManager.exportReportToPdfStream(
          jasperPrint,
          response.getOutputStream()
        );
    }
    
    
    
    @Autowired
    private HistorialMedicoService historialService;  // inyecta tu servicio de historiales

    @GetMapping("/citas/historial/pdf/{id}")
    public void historialPdfClinica(
        @PathVariable("id") Integer citaId,
        HttpServletResponse response
    ) throws Exception {
        // 1) Obtener el historial y la cita
        var historial = historialService.findByCitaId(citaId);
        var cita      = historial.getCita();

        // 2) Cargar el .jasper
        ClassPathResource jasperFile = new ClassPathResource("informes/HistorialMedico.jasper");
        JasperReport report = (JasperReport) JRLoader.loadObject(jasperFile.getInputStream());

        // 3) Llenar el mapa con los campos
        Map<String,Object> row = new HashMap<>();
        row.put("pacienteNombre", cita.getPacienteNombre());
        row.put("medicoNombre",   cita.getMedicoNombre());
        row.put("clinicaNombre",  cita.getClinicaNombre());
        row.put("fechaCita",      java.sql.Timestamp.valueOf(cita.getFechaHora()));
        row.put("fechaImpresion", java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
        row.put("diagnostico",    historial.getDiagnostico());
        row.put("receta",         historial.getReceta());

        JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(List.of(row));

        // 4) Rellenar y exportar
        JasperPrint jp = JasperFillManager.fillReport(report, new HashMap<>(), ds);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
            "inline; filename=historial_cita_" + citaId + ".pdf");
        JasperExportManager.exportReportToPdfStream(jp, response.getOutputStream());
    }

    
    
    
    
    
    
    
    
    
    
    
    

    @GetMapping("/citas/historial/{id}")
    public String verHistorialCita(
        @PathVariable("id") Integer citaId,
        @AuthenticationPrincipal UserDetails ud,
        Model model,
        RedirectAttributes ra
    ) {
        Usuario usuLog = usuarioService.buscarPorLogin(ud.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
        Clinica clinica = clinicaRepository.findByUsuarioId(usuLog.getId())
            .orElseThrow(() -> new RuntimeException("Clínica no encontrada."));
        Cita cita = citaService.findById(citaId);

        if (!cita.getClinica().getId().equals(clinica.getId())) {
            ra.addFlashAttribute("errorMsg", "No tienes permiso para ver este historial.");
            return "redirect:/clinica/citas";
        }
        if (cita.getEstado() != Cita.Estado.REALIZADA || cita.getHistorial() == null) {
            ra.addFlashAttribute("errorMsg", "El historial aún no está disponible.");
            return "redirect:/clinica/citas";
        }

        model.addAttribute("cita", cita);
        model.addAttribute("historial", cita.getHistorial());
        return "clinica/historial-cita";
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
