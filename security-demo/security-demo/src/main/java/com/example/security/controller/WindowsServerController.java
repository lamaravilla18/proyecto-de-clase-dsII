package com.example.security.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.security.servicios.WindowsService;

@RestController
@RequestMapping("/api/windows-server")
@CrossOrigin(origins = "*")
public class WindowsServerController {
    
    @Autowired
    private WindowsService windowsService;
    
    /**
     * Verificar conectividad con Windows Server
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getServerStatus() {
        Map<String, Object> status = windowsService.checkServerConnectivity();
        return ResponseEntity.ok(status);
    }
    
    /**
     * Obtener estado de servicios
     */
    @GetMapping("/services")
    public ResponseEntity<Map<String, Object>> getServices() {
        Map<String, Object> services = windowsService.getWindowsServices();
        return ResponseEntity.ok(services);
    }
    
    /**
     * Obtener uso de recursos
     */
    @GetMapping("/resources")
    public ResponseEntity<Map<String, Object>> getResources() {
        Map<String, Object> resources = windowsService.getServerResources();
        return ResponseEntity.ok(resources);
    }
}