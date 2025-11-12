// src/main/java/com/example/gestioncitas/dto/UsuarioDto.java
package com.co.gestiondecitasmedicas.dto;

import java.util.Set;

public class UsuarioDto {

    private String nombre;
    private String usuariologin;
    private String documento;
    private String email;
    private String telefono;
    private String direccion;
    private String password;

    // ids de roles seleccionados en el formulario (ej. 1, 2 o 3)
    private Set<Integer> rolesIds;

    // Constructores, getters y setters
    public UsuarioDto() {}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUsuariologin() { return usuariologin; }
    public void setUsuariologin(String usuariologin) { this.usuariologin = usuariologin; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Set<Integer> getRolesIds() { return rolesIds; }
    public void setRolesIds(Set<Integer> rolesIds) { this.rolesIds = rolesIds; }
}
