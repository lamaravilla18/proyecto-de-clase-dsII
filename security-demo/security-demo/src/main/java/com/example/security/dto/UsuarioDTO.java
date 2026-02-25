package com.example.security.dto;

import java.util.List;

public class UsuarioDTO {

    private Long id; // ✅ AGREGAR ESTE CAMPO
    private String username;
    private String password;
    private String nombre;
    private String apellido;
    private String email;
    private List<String> roles;
    private boolean activo;

    // Constructor vacío
    public UsuarioDTO() {
    }

    // Constructor con todos los campos
    public UsuarioDTO(Long id, String username, String password, String nombre, String apellido,
                      String email, List<String> roles, boolean activo) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.roles = roles;
        this.activo = activo;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // toString (opcional)
    @Override
    public String toString() {
        return "UsuarioDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", email='" + email + '\'' +
                ", roles=" + roles +
                ", activo=" + activo +
                '}';
    }
}