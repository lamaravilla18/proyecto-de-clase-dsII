package com.example.security.controller;

import com.example.security.dto.QoSData;
import com.example.security.modelo.QoSMetrics;
import com.example.security.servicios.QoSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/network/qos")
@CrossOrigin(origins = "*")
public class QoSController {

    @Autowired
    private QoSService qosService;

    /**
     * POST /api/network/qos/measure
     * Medición manual de QoS
     */
    @PostMapping("/measure")
public ResponseEntity<QoSData> measureQoS(@RequestBody Map<String, Object> request) {
    try {
        String ip = (String) request.get("ip");
        
        // ✅ CORRECCIÓN: Manejar correctamente el tipo de "packets"
        int packets = 20; // valor por defecto
        if (request.containsKey("packets")) {
            Object packetsObj = request.get("packets");
            if (packetsObj instanceof Integer) {
                packets = (Integer) packetsObj;
            } else if (packetsObj instanceof String) {
                packets = Integer.parseInt((String) packetsObj);
            }
        }
        
        // Validar IP
        if (ip == null || ip.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Medir QoS
        QoSData result = qosService.measureQoS(ip, packets);
        result.setTargetIp(ip);
        result.setDuringActiveCall(false); // Manual = sin llamada
        
        return ResponseEntity.ok(result);
        
    } catch (Exception e) {
        // Log del error para debugging
        System.err.println("Error en measureQoS: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.internalServerError().build();
    }
}

    /**
     * GET /api/network/qos/latest
     * Obtiene las últimas 50 métricas
     */
    @GetMapping("/latest")
    public ResponseEntity<List<QoSMetrics>> getLatestMetrics() {
        List<QoSMetrics> metrics = qosService.getRecentMetrics(24);
        return ResponseEntity.ok(metrics);
    }

    
    @GetMapping("/mos-average")
    public ResponseEntity<Map<String, Double>> getAverageMos() {
        Double avgMos = qosService.getAverageMosScore();
        return ResponseEntity.ok(Map.of("averageMos", avgMos != null ? avgMos : 0.0));
    }

    /**
     * ⭐ NUEVO ENDPOINT: Comparación de métricas
     * GET /api/network/qos/comparison
     */
    @GetMapping("/comparison")
    public ResponseEntity<Map<String, Object>> getComparison() {
        Map<String, Object> comparison = qosService.getComparativeMetrics();
        return ResponseEntity.ok(comparison);
    }
}