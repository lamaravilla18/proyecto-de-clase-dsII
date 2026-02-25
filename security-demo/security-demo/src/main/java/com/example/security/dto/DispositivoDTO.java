package com.example.security.dto;

import com.example.security.modelo.EstadoAutorizacion;

public class DispositivoDTO {
    private String ip;
    private String timestamp;
    private EstadoAutorizacion estadoAutorizacion;
    
    public String getIp() {
        return ip;
    }
    
    public void setIp(String ip) {
        this.ip = ip;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public EstadoAutorizacion getEstadoAutorizacion() {
        return estadoAutorizacion;
    }
    
    public void setEstadoAutorizacion(EstadoAutorizacion estadoAutorizacion) {
        this.estadoAutorizacion = estadoAutorizacion;
    }
}