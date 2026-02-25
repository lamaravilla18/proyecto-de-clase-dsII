package com.example.security.servicios;

import com.example.security.dto.QoSData;
import com.example.security.modelo.QoSMetrics;
import com.example.security.repositorio.QoSMetricsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class QoSService {
    
    private static final Logger logger = LoggerFactory.getLogger(QoSService.class);
    
    @Autowired
    private QoSMetricsRepository qosRepository;
    
    /**
     * Mide QoS usando ping extendido (latencia, jitter, pérdida de paquetes)
     */
    public QoSData measureQoS(String targetIp, int packetCount) {
        try {
            // Construir comando ping según el SO
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            
            if (os.contains("win")) {
                pb = new ProcessBuilder("ping", "-n", String.valueOf(packetCount), targetIp);
            } else {
                pb = new ProcessBuilder("ping", "-c", String.valueOf(packetCount), targetIp);
            }
            
            logger.info("Ejecutando ping a {} con {} paquetes (OS: {})", targetIp, packetCount, os);
            
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            List<Double> rttValues = new ArrayList<>();
            int sent = packetCount;
            int received = 0;
            
            String line;
            while ((line = reader.readLine()) != null) {
                logger.debug("Línea ping: {}", line);
                
                Double rtt = extractRTT(line);
                if (rtt != null) {
                    rttValues.add(rtt);
                    received++;
                    logger.debug("RTT encontrado: {}ms", rtt);
                }
            }
            
            process.waitFor();
            
            logger.info("Ping completado - Enviados: {}, Recibidos: {}, RTTs capturados: {}", 
                        sent, received, rttValues.size());
            
            // Calcular métricas
            QoSData qosData = calculateMetrics(rttValues, sent, received);
            
            // Guardar en BD
            saveQoSMetrics(targetIp, qosData, "ICMP");
            
            return qosData;
            
        } catch (IOException | InterruptedException e) {
            logger.error("Error al medir QoS para {}: {}", targetIp, e.getMessage());
            return createErrorQoSData();
        }
    }
    
    /**
     * Extrae el RTT de una línea de salida de ping
     */
    private Double extractRTT(String line) {
        if (line.contains("time") || line.contains("tiempo")) {
            logger.debug("Analizando línea con tiempo: {}", line);
        }
        
        // Intentar varios patrones
        Pattern[] patterns = {
            Pattern.compile("time[=<](\\d+(?:\\.\\d+)?)\\s*ms", Pattern.CASE_INSENSITIVE),
            Pattern.compile("time=(\\d+(?:\\.\\d+)?)\\s*ms", Pattern.CASE_INSENSITIVE),
            Pattern.compile("tiempo[=<](\\d+(?:\\.\\d+)?)\\s*ms", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*ms")
        };
        
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                try {
                    double rtt = Double.parseDouble(matcher.group(1));
                    logger.debug("✅ RTT extraído: {}ms usando patrón: {}", rtt, pattern.pattern());
                    return rtt;
                } catch (NumberFormatException e) {
                    logger.warn("Error parseando RTT: {}", matcher.group(1));
                }
            }
        }
        
        return null;
    }
    
    /**
     * Calcula todas las métricas QoS
     */
    private QoSData calculateMetrics(List<Double> rttValues, int sent, int received) {
        QoSData qosData = new QoSData();
        
        if (rttValues.isEmpty() || received == 0) {
            logger.warn("No se recibieron respuestas de ping. Paquetes enviados: {}, recibidos: {}", sent, received);
            return createErrorQoSData();
        }
        
        // 1. LATENCIA (promedio de RTT)
        double sum = 0;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        
        for (double rtt : rttValues) {
            sum += rtt;
            if (rtt < min) min = rtt;
            if (rtt > max) max = rtt;
        }
        
        double avgLatency = sum / rttValues.size();
        qosData.setLatencyMs(avgLatency);
        qosData.setRttAvgMs(avgLatency);
        qosData.setRttMinMs(min);
        qosData.setRttMaxMs(max);
        
        // 2. JITTER (variación de latencia)
        double jitter = calculateJitter(rttValues);
        qosData.setJitterMs(jitter);
        
        // 3. PÉRDIDA DE PAQUETES
        int lost = sent - received;
        double packetLoss = (lost / (double) sent) * 100.0;
        qosData.setPacketLossPercent(packetLoss);
        qosData.setPacketsSent(sent);
        qosData.setPacketsReceived(received);
        qosData.setPacketsLost(lost);
        
        // 4. MOS SCORE (calidad de voz)
        double mosScore = calculateMOSScore(avgLatency, jitter, packetLoss);
        qosData.setMosScore(mosScore);
        
        // 5. QUALITY STATUS
        String qualityStatus = determineQualityStatus(mosScore);
        qosData.setQualityStatus(qualityStatus);
        
        // ✅ CORRECCIÓN: Sin {:.2f} en logger
        logger.info("QoS calculado - Latencia: {}ms, Jitter: {}ms, Pérdida: {}%, MOS: {}, Estado: {}", 
                    String.format("%.2f", avgLatency), 
                    String.format("%.2f", jitter), 
                    String.format("%.2f", packetLoss), 
                    String.format("%.2f", mosScore), 
                    qualityStatus);
        
        return qosData;
    }

    /**
     * Determina el estado de calidad según el MOS Score
     */
    private String determineQualityStatus(double mosScore) {
        if (mosScore >= 4.3) {
            return "EXCELLENT";
        } else if (mosScore >= 4.0) {
            return "GOOD";
        } else if (mosScore >= 3.6) {
            return "FAIR";
        } else if (mosScore >= 3.1) {
            return "POOR";
        } else {
            return "BAD";
        }
    }
    
    /**
     * Calcula el Jitter (variación del delay)
     */
    private double calculateJitter(List<Double> rttValues) {
        if (rttValues.size() < 2) {
            return 0.0;
        }
        
        double jitterSum = 0.0;
        for (int i = 1; i < rttValues.size(); i++) {
            jitterSum += Math.abs(rttValues.get(i) - rttValues.get(i - 1));
        }
        
        return jitterSum / (rttValues.size() - 1);
    }
    
    /**
     * Calcula el MOS Score (Mean Opinion Score)
     */
    private double calculateMOSScore(double latency, double jitter, double packetLoss) {
        double r = 93.2;
        
        double latencyPenalty;
        if (latency < 160) {
            latencyPenalty = latency / 40.0;
        } else {
            latencyPenalty = (latency - 120) / 10.0;
        }
        
        double jitterPenalty = jitter / 2.0;
        double packetLossPenalty = packetLoss * 2.5;
        
        r = r - latencyPenalty - jitterPenalty - packetLossPenalty;
        
        if (r < 0) r = 0;
        if (r > 100) r = 100;
        
        double mos;
        if (r < 0) {
            mos = 1.0;
        } else if (r > 100) {
            mos = 4.5;
        } else {
            mos = 1 + 0.035 * r + 7 * Math.pow(10, -6) * r * (r - 60) * (100 - r);
        }
        
        if (mos < 1.0) mos = 1.0;
        if (mos > 5.0) mos = 5.0;
        
        return Math.round(mos * 100.0) / 100.0;
    }
    
    /**
     * Guarda las métricas en la base de datos
     */
    private void saveQoSMetrics(String targetIp, QoSData qosData, String protocolType) {
        try {
            QoSMetrics metrics = new QoSMetrics();
            metrics.setTargetIp(targetIp);
            metrics.setLatencyMs(qosData.getLatencyMs());
            metrics.setJitterMs(qosData.getJitterMs());
            metrics.setPacketLossPercent(qosData.getPacketLossPercent());
            metrics.setMosScore(qosData.getMosScore());
            metrics.setRttAvgMs(qosData.getRttAvgMs());
            metrics.setRttMinMs(qosData.getRttMinMs());
            metrics.setRttMaxMs(qosData.getRttMaxMs());
            metrics.setPacketsSent(qosData.getPacketsSent());
            metrics.setPacketsReceived(qosData.getPacketsReceived());
            metrics.setPacketsLost(qosData.getPacketsLost());
            metrics.setProtocolType(protocolType);
            metrics.setQualityStatus(qosData.getQualityStatus());
            
            qosRepository.save(metrics);
            logger.info("Métricas QoS guardadas para {}: {}", targetIp, qosData);
        } catch (Exception e) {
            logger.error("Error al guardar métricas QoS: {}", e.getMessage());
        }
    }
    
    /**
     * Crea un objeto QoSData con valores de error
     */
    private QoSData createErrorQoSData() {
        QoSData qosData = new QoSData();
        qosData.setLatencyMs(0.0);
        qosData.setJitterMs(0.0);
        qosData.setPacketLossPercent(100.0);
        qosData.setMosScore(1.0);
        qosData.setQualityStatus("BAD");
        return qosData;
    }
    
    /**
     * Obtiene métricas históricas
     */
    public List<QoSMetrics> getRecentMetrics(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return qosRepository.findRecentMetrics(since);
    }
    
    /**
     * Obtiene métricas por IP
     */
    public List<QoSMetrics> getMetricsByIp(String targetIp) {
        return qosRepository.findByTargetIpOrderByTimestampDesc(targetIp);
    }
    
    /**
     * Obtiene promedio de MOS de las últimas 24 horas
     */
    public Double getAverageMosScore() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return qosRepository.getAverageMosScore(since);
    }

    /**
     * Actualiza el flag duringActiveCall de la última métrica guardada
     */
    public void updateLastMetricCallStatus(boolean duringCall) {
        try {
            List<QoSMetrics> lastMetrics = qosRepository.findTop10ByOrderByTimestampDesc();
            if (!lastMetrics.isEmpty()) {
                QoSMetrics lastMetric = lastMetrics.get(0);
                lastMetric.setDuringActiveCall(duringCall);
                qosRepository.save(lastMetric);
                logger.debug("Flag duringActiveCall actualizado a {} para métrica ID {}", 
                            duringCall, lastMetric.getId());
            }
        } catch (Exception e) {
            logger.error("Error actualizando flag duringActiveCall: {}", e.getMessage());
        }
    }

    /**
     * Obtiene métricas comparativas (con llamada vs sin llamada)
     */
    public Map<String, Object> getComparativeMetrics() {
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        
        // Promedios durante llamadas activas
        Double avgMosDuringCalls = qosRepository.getAverageMosDuringCalls(last24Hours);
        
        // Promedios sin llamadas activas
        Double avgMosWithoutCalls = qosRepository.getAverageMosWithoutCalls(last24Hours);
        
        // Contadores
        List<QoSMetrics> allMetrics = qosRepository.findTop100ByOrderByTimestampDesc();
        long duringCallCount = allMetrics.stream().filter(m -> m.getDuringActiveCall() != null && m.getDuringActiveCall()).count();
        long withoutCallCount = allMetrics.stream().filter(m -> m.getDuringActiveCall() == null || !m.getDuringActiveCall()).count();
        
        // ✅ CORRECCIÓN: Usar HashMap en lugar de Map.of() para mayor compatibilidad
        Map<String, Object> result = new HashMap<>();
        result.put("averageMosDuringCalls", avgMosDuringCalls != null ? avgMosDuringCalls : 0.0);
        result.put("averageMosWithoutCalls", avgMosWithoutCalls != null ? avgMosWithoutCalls : 0.0);
        result.put("measurementsDuringCalls", duringCallCount);
        result.put("measurementsWithoutCalls", withoutCallCount);
        result.put("totalMeasurements", allMetrics.size());
        
        return result;
    }
}