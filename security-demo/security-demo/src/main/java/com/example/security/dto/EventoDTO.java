package com.example.security.dto;

public class EventoDTO {

    private String descripcion;

    // Constructor vacío
    public EventoDTO() {
    }

    // Constructor con parámetro
    public EventoDTO(String descripcion) {
        this.descripcion = descripcion;
    }

    // Getter y Setter
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // toString (opcional)
    @Override
    public String toString() {
        return "EventoDTO{" +
                "descripcion='" + descripcion + '\'' +
                '}';
    }
}
