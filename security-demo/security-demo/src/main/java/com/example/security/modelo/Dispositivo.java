package com.example.security.modelo;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Entity
@Table(name = "dispositivos")
public class Dispositivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ip;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ultima_conexion")
    private Date ultimaConexion;
    
    // Campo para almacenar el estado de autorización como un String en la BD
    @Column(name = "estado_autorizacion", length = 20)
    private String estadoAutorizacionStr;
    
    // Propiedad transitoria para manejar el enum
    @Transient
    private com.example.security.modelo.EstadoAutorizacion estadoAutorizacion;

    // Constructor vacío
    public Dispositivo() {
    }

    public Dispositivo(String ip, Date ultimaConexion) {
        this.ip = ip;
        this.ultimaConexion = ultimaConexion;
        this.calcularEstadoAutorizacion();
    }

    // Getters y Setters
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
        this.calcularEstadoAutorizacion();
    }
    
    public Date getUltimaConexion() {
        return ultimaConexion;
    }

    public void setUltimaConexion(Date ultimaConexion) {
        this.ultimaConexion = ultimaConexion;
    }
    
    public EstadoAutorizacion getEstadoAutorizacion() {
        if (estadoAutorizacion == null && estadoAutorizacionStr != null) {
            try {
                estadoAutorizacion = EstadoAutorizacion.valueOf(estadoAutorizacionStr);
            } catch (IllegalArgumentException e) {
                estadoAutorizacion = EstadoAutorizacion.DESCONOCIDO;
            }
        }
        return estadoAutorizacion != null ? estadoAutorizacion : EstadoAutorizacion.DESCONOCIDO;
    }
    
    public void setEstadoAutorizacion(EstadoAutorizacion estadoAutorizacion) {
        this.estadoAutorizacion = estadoAutorizacion;
        this.estadoAutorizacionStr = estadoAutorizacion != null ? estadoAutorizacion.name() : null;
    }
    
    // Getters y setters para el campo de base de datos
    public String getEstadoAutorizacionStr() {
        return estadoAutorizacionStr;
    }
    
    public void setEstadoAutorizacionStr(String estadoAutorizacionStr) {
        this.estadoAutorizacionStr = estadoAutorizacionStr;
        if (estadoAutorizacionStr != null) {
            try {
                this.estadoAutorizacion = EstadoAutorizacion.valueOf(estadoAutorizacionStr);
            } catch (IllegalArgumentException e) {
                this.estadoAutorizacion = EstadoAutorizacion.DESCONOCIDO;
            }
        }
    }
    
    private void calcularEstadoAutorizacion() {
        try {
            if (ip == null || ip.isEmpty()) {
                this.estadoAutorizacion = EstadoAutorizacion.DESCONOCIDO;
                this.estadoAutorizacionStr = EstadoAutorizacion.DESCONOCIDO.name();
                return;
            }
            
            // Verificar si la IP está en el formato correcto
            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                this.estadoAutorizacion = EstadoAutorizacion.DESCONOCIDO;
                this.estadoAutorizacionStr = EstadoAutorizacion.DESCONOCIDO.name();
                return;
            }
            
            // Solo consideramos IPs en el rango 192.168.1.X
            if (!parts[0].equals("192") || !parts[1].equals("168") || !parts[2].equals("1")) {
                this.estadoAutorizacion = EstadoAutorizacion.DESCONOCIDO;
                this.estadoAutorizacionStr = EstadoAutorizacion.DESCONOCIDO.name();
                return;
            }
            
            int lastOctet = Integer.parseInt(parts[3]);
            
            // Reglas de clasificación según los rangos solicitados
            if (lastOctet >= 1 && lastOctet <= 3) {
                this.estadoAutorizacion = EstadoAutorizacion.AUTORIZADO;
                this.estadoAutorizacionStr = EstadoAutorizacion.AUTORIZADO.name();
            } else if (lastOctet >= 4 && lastOctet <= 6) {
                this.estadoAutorizacion = EstadoAutorizacion.NO_AUTORIZADO;
                this.estadoAutorizacionStr = EstadoAutorizacion.NO_AUTORIZADO.name();
            } else if (lastOctet >= 7 && lastOctet <= 10) {
                this.estadoAutorizacion = EstadoAutorizacion.DESCONOCIDO;
                this.estadoAutorizacionStr = EstadoAutorizacion.DESCONOCIDO.name();
            } else {
                this.estadoAutorizacion = EstadoAutorizacion.DESCONOCIDO;
                this.estadoAutorizacionStr = EstadoAutorizacion.DESCONOCIDO.name();
            }
        } catch (Exception e) {
            this.estadoAutorizacion = EstadoAutorizacion.DESCONOCIDO;
            this.estadoAutorizacionStr = EstadoAutorizacion.DESCONOCIDO.name();
        }
    }
    
    // Añadir métodos para calcular automaticamente el estado antes de persistir o actualizar
    @PrePersist
    @PreUpdate
    public void prePersist() {
        calcularEstadoAutorizacion();
    }
}