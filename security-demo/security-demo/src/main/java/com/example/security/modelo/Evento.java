package com.example.security.modelo;

import java.text.SimpleDateFormat;
import java.util.Date;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "eventos")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ip;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date fecha;

    @Temporal(TemporalType.TIME)
    @Column(nullable = false)
    private Date hora;

    
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public Evento(String ip, String descripcion) {
        this.ip = ip;
        this.fecha = new Date(); // Fecha actual
        this.hora = new Date(); 
        this.descripcion = descripcion;
    }

    // Getters y setters
    public Evento() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Date getHora() {
        return hora;
    }

    public void setHora(Date hora) {
        this.hora = hora;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    public Usuario getUsuario() {
    return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
    }

    // Métodos formateados (opcional)
    public String getFechaFormateada() {
        return new SimpleDateFormat("dd/MM/yyyy").format(this.fecha);
    }

    public String getHoraFormateada() {
        return new SimpleDateFormat("HH:mm:ss").format(this.hora);
    }
}
