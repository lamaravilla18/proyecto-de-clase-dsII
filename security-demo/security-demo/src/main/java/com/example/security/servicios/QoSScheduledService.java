package com.example.security.servicios;

import com.example.security.dto.QoSData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class QoSScheduledService {
    
    private static final Logger logger = LoggerFactory.getLogger(QoSScheduledService.class);
    
    @Autowired
    private AsteriskAMIService asteriskService;
    
    @Autowired
    private QoSService qosService;
    
    // IP objetivo para mediciones (puedes hacerlo configurable)
    private static final String TARGET_IP = "8.8.8.8"; // Google DNS
    private static final int PACKET_COUNT = 10; // Menos paquetes para ser rápido
    
    /**
     * ⭐ TAREA PROGRAMADA: Se ejecuta cada 10 segundos
     * Mide QoS automáticamente cuando hay llamadas VoIP activas
     */
    @Scheduled(fixedDelay = 10000) // Cada 10 segundos
    public void monitorQoSDuringActiveCalls() {
        try {
            // 1. Verificar si el AMI está conectado
            if (!asteriskService.isConnected()) {
                logger.debug("AMI no conectado, saltando medición automática");
                return;
            }
            
            // 2. Obtener llamadas activas
            String channelsResponse = asteriskService.getCoreShowChannels();
            int activeCalls = extractActiveCallCount(channelsResponse);
            
            // 3. Si hay llamadas activas, medir QoS
            if (activeCalls > 0) {
                logger.info("📞 {} llamada(s) activa(s) detectada(s). Midiendo QoS automáticamente...", activeCalls);
                
                // Medir QoS y marcarlo como "durante llamada"
                QoSData qosData = qosService.measureQoS(TARGET_IP, PACKET_COUNT);
                qosData.setDuringActiveCall(true);
                qosData.setTargetIp(TARGET_IP);
                
                
                // ✅ CORRECCIÓN: Formatear manualmente o usar String.format()
                logger.info("✅ QoS medido durante llamada - MOS: {}, Estado: {}", 
                           String.format("%.2f", qosData.getMosScore()), 
                           qosData.getQualityStatus());
            }
            
        } catch (Exception e) {
            logger.error("Error en monitoreo automático de QoS: {}", e.getMessage());
        }
    }
    
    /**
     * Extrae el número de llamadas activas del response del AMI
     */
    private int extractActiveCallCount(String response) {
        try {
            String[] lines = response.split("\n");
            for (String line : lines) {
                if (line.startsWith("ListItems:")) {
                    String count = line.substring(10).trim();
                    return Integer.parseInt(count);
                }
            }
        } catch (Exception e) {
            logger.warn("Error extrayendo conteo de llamadas: {}", e.getMessage());
        }
        return 0;
    }
}