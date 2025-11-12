// src/main/java/com/co/gestiondecitasmedicas/models/Clinica.java
package com.co.gestiondecitasmedicas.models;

import java.util.Set;
import jakarta.persistence.*;

@Entity
@Table(name = "clinicas")
public class Clinica {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nombre;

    // Usuario que actúa como la cuenta de la clínica (rol CLINICA)
    @OneToOne
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    // Médicos que pertenecen a esta clínica
    @OneToMany(mappedBy = "clinica", cascade = CascadeType.ALL)
    private Set<Usuario> medicos;

    // getters y setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Set<Usuario> getMedicos() { return medicos; }
    public void setMedicos(Set<Usuario> medicos) { this.medicos = medicos; }
}
