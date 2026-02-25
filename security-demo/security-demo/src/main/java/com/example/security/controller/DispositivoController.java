package com.example.security.controller;

import com.example.security.modelo.Dispositivo;
import com.example.security.dto.DispositivoDTO;
import com.example.security.servicios.DispositivoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/dispositivos")
public class DispositivoController {

    @Autowired
    private DispositivoService dispositivoService;

    /**
     * Obtiene todos los dispositivos
     */
    @GetMapping
    public ResponseEntity<List<Dispositivo>> getAllDispositivos() {
        List<Dispositivo> dispositivos = dispositivoService.findAll();
        return new ResponseEntity<>(dispositivos, HttpStatus.OK);
    }

    /**
     * Registra un nuevo dispositivo o actualiza uno existente
     */
    @PostMapping
    public ResponseEntity<?> registrarDispositivo(@RequestBody DispositivoDTO dispositivoDTO) {
        try {
            // Usar fecha y hora actual del servidor
            Date timestamp = new Date();

            // Crear objeto Dispositivo con los datos recibidos
            Dispositivo dispositivo = new Dispositivo();
            dispositivo.setIp(dispositivoDTO.getIp());
            dispositivo.setUltimaConexion(timestamp);

            // Guardar o actualizar el dispositivo
            Dispositivo savedDispositivo = dispositivoService.saveDispositivo(dispositivo);
            return new ResponseEntity<>(savedDispositivo, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(
                "Error al procesar la solicitud: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Buscar dispositivo por IP
     */
    @GetMapping("/ip/{ip}")
    public ResponseEntity<Dispositivo> getDispositivoByIp(@PathVariable String ip) {
        Dispositivo dispositivo = dispositivoService.findByIp(ip);
        if (dispositivo != null) {
            return new ResponseEntity<>(dispositivo, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
