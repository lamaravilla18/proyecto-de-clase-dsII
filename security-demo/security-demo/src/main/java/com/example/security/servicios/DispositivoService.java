package com.example.security.servicios;

import com.example.security.modelo.Dispositivo;
import com.example.security.repositorio.DispositivoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DispositivoService {
    
    @Autowired
    private DispositivoRepository dispositivoRepository;
    
    /**
     * Devuelve todos los dispositivos
     */
    public List<Dispositivo> findAll() {
        return dispositivoRepository.findAll();
    }
    
    /**
     * Guarda o actualiza un dispositivo
     */
    public Dispositivo saveDispositivo(Dispositivo dispositivo) {
        // Verificar si el dispositivo ya existe por IP
        Dispositivo existingByIp = dispositivoRepository.findByIp(dispositivo.getIp());
        if (existingByIp != null) {
            // Si existe, actualizar la última conexión
            existingByIp.setUltimaConexion(dispositivo.getUltimaConexion());
            return dispositivoRepository.save(existingByIp);
        } else {
            // Si no existe, guardar como nuevo
            return dispositivoRepository.save(dispositivo);
        }
    }
    
    /**
     * Busca un dispositivo por IP
     */
    public Dispositivo findByIp(String ip) {
        return dispositivoRepository.findByIp(ip);
    }
    
}