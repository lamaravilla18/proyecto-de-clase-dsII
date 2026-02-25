package com.example.security.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.security.dto.QoSData;
import com.example.security.modelo.QoSMetrics;
import com.example.security.servicios.NetworkTrafficService;
import com.example.security.servicios.QoSService;

@RestController
@RequestMapping("/api/network")
public class NetworkTrafficController {

    private static final Logger logger = LoggerFactory.getLogger(NetworkTrafficController.class);
    
    @Autowired
    private NetworkTrafficService service;
    
    @Autowired
    private QoSService qosService;
    
    /**
     * Método compatible con tu implementación original
     * Mantenido para retrocompatibilidad
     */
    @GetMapping("/admin/network-usage")
    public SseEmitter streamTrafficDataLegacy() {
        SseEmitter emitter = new SseEmitter(0L); // sin timeout
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                service.startCapturing(data -> {
                    try {
                        emitter.send(data);
                    } catch (IOException e) {
                        // Cliente desconectado o error al enviar datos
                        emitter.completeWithError(e);
                    }
                });
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        
        emitter.onCompletion(() -> {
            logger.info("SSE completado");
            executor.shutdownNow();
        });
        
        emitter.onTimeout(() -> {
            logger.info("SSE timeout");
            emitter.complete();
            executor.shutdownNow();
        });
        
        emitter.onError((Throwable t) -> {
            logger.error("SSE error: " + t.getMessage());
            executor.shutdownNow();
        });
        
        return emitter;
    }
    
    /**
     * Proporciona un SSE para actualizaciones en tiempo real
     * Utilizando el nuevo método 'subscribe' del servicio
     */
    @GetMapping("/monitor")
    public SseEmitter monitorTraffic() {
        logger.info("Nueva conexión SSE para monitoreo de tráfico");
        return service.subscribe();
    }
    
    /**
     * Obtiene los datos actuales sin SSE (para cuando regresa a la página)
     */
    @GetMapping("/data")
    public ResponseEntity<NetworkTrafficService.TrafficData> getCurrentData() {
        return ResponseEntity.ok(service.getCurrentTrafficData());
    }
    
    /**
     * Inicia la captura de tráfico
     */
    @PostMapping("/start")
    public ResponseEntity<String> startCapturing() {
        try {
            service.startCapturing(null); // Sin Consumer para usar solo SSE
            return ResponseEntity.ok("Captura de tráfico iniciada");
        } catch (Exception e) {
            logger.error("Error al iniciar la captura de tráfico", e);
            return ResponseEntity.internalServerError()
                    .body("Error al iniciar la captura de tráfico: " + e.getMessage());
        }
    }
    
    /**
     * Detiene la captura de tráfico
     */
    @PostMapping("/stop")
    public ResponseEntity<String> stopCapturing() {
        service.stopCapturing();
        return ResponseEntity.ok("Captura de tráfico detenida");
    }
    
    /**
     * Reinicia el contador de bytes
     */
    @PostMapping("/reset")
    public ResponseEntity<String> resetCounter() {
        service.resetCounter();
        return ResponseEntity.ok("Contador reiniciado");
    }
    
    
    /**
     * Obtener historial de métricas QoS de las últimas N horas
     * 
     * @param hours Número de horas hacia atrás (por defecto 24)
     * @return Lista de métricas QoS guardadas
     */
    @GetMapping("/qos/history")
    public ResponseEntity<List<QoSMetrics>> getQoSHistory(
            @RequestParam(defaultValue = "24") int hours) {
        logger.info("Obteniendo historial QoS de las últimas {} horas", hours);
        List<QoSMetrics> metrics = qosService.getRecentMetrics(hours);
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Obtener todas las métricas QoS registradas para una IP específica
     * 
     * @param ip Dirección IP a consultar
     * @return Lista de métricas QoS para esa IP
     */
    @GetMapping("/qos/by-ip/{ip}")
    public ResponseEntity<List<QoSMetrics>> getQoSByIp(@PathVariable String ip) {
        logger.info("Obteniendo métricas QoS para IP: {}", ip);
        List<QoSMetrics> metrics = qosService.getMetricsByIp(ip);
        return ResponseEntity.ok(metrics);
    }
    
    
    /**
     * Realizar múltiples mediciones QoS a diferentes hosts
     * 
     * @param request JSON con: { "ips": ["192.168.1.1", "8.8.8.8"], "packets": 10 }
     * @return Mapa con las métricas QoS de cada IP
     */
    @PostMapping("/qos/measure-multiple")
    public ResponseEntity<Map<String, QoSData>> measureMultipleQoS(
            @RequestBody Map<String, Object> request) {
        
        @SuppressWarnings("unchecked")
        List<String> ips = (List<String>) request.get("ips");
        int packetCount = request.containsKey("packets") ? 
                          (Integer) request.get("packets") : 10;
        
        if (ips == null || ips.isEmpty()) {
            logger.warn("Intento de medición múltiple sin IPs");
            return ResponseEntity.badRequest().body(null);
        }
        
        logger.info("Iniciando mediciones QoS múltiples para {} IPs", ips.size());
        
        Map<String, QoSData> results = new java.util.HashMap<>();
        for (String ip : ips) {
            QoSData qosData = qosService.measureQoS(ip, packetCount);
            results.put(ip, qosData);
        }
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * Endpoint de diagnóstico para verificar estado del servicio QoS
     */
    @GetMapping("/qos/status")
    public ResponseEntity<Map<String, Object>> getQoSServiceStatus() {
        Map<String, Object> status = new java.util.HashMap<>();
        status.put("service", "QoS Measurement Service");
        status.put("status", "ACTIVE");
        status.put("version", "1.0.0");
        
        try {
            // Verificar que el servicio QoS esté funcionando
            Double avgMos = qosService.getAverageMosScore();
            status.put("database", "CONNECTED");
            status.put("lastAverageMos", avgMos);
        } catch (Exception e) {
            status.put("database", "ERROR");
            status.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(status);
    }
}