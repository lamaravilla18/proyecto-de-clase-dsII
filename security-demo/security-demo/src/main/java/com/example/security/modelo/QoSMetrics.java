package com.example.security.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "qos_metrics")
public class QoSMetrics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    @Column(name = "target_ip")
    private String targetIp;
    
    // Latencia (Delay) en milisegundos
    @Column(name = "latency_ms")
    private Double latencyMs;
    
    // Jitter en milisegundos
    @Column(name = "jitter_ms")
    private Double jitterMs;
    
    // Pérdida de paquetes en porcentaje
    @Column(name = "packet_loss_percent")
    private Double packetLossPercent;
    
    // MOS Score (Mean Opinion Score) - Calidad de voz (1.0 - 5.0)
    @Column(name = "mos_score")
    private Double mosScore;
    
    // RTT (Round Trip Time) promedio en ms
    @Column(name = "rtt_avg_ms")
    private Double rttAvgMs;
    
    // RTT mínimo
    @Column(name = "rtt_min_ms")
    private Double rttMinMs;
    
    // RTT máximo
    @Column(name = "rtt_max_ms")
    private Double rttMaxMs;
    
    // Tipo de protocolo analizado
    @Column(name = "protocol_type")
    private String protocolType; // VoIP, HTTP, Streaming, Other
    
    // Estado de calidad
    @Column(name = "quality_status")
    private String qualityStatus; // EXCELLENT, GOOD, FAIR, POOR, BAD
    
    // Paquetes enviados
    @Column(name = "packets_sent")
    private Integer packetsSent;
    
    // Paquetes recibidos
    @Column(name = "packets_received")
    private Integer packetsReceived;
    
    // Paquetes perdidos
    @Column(name = "packets_lost")
    private Integer packetsLost;

    @Column(name = "during_active_call")
    private Boolean duringActiveCall = false;

    // Constructores
    public QoSMetrics() {
        this.timestamp = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getTargetIp() {
        return targetIp;
    }

    public void setTargetIp(String targetIp) {
        this.targetIp = targetIp;
    }

    public Double getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Double latencyMs) {
        this.latencyMs = latencyMs;
    }

    public Double getJitterMs() {
        return jitterMs;
    }

    public void setJitterMs(Double jitterMs) {
        this.jitterMs = jitterMs;
    }

    public Double getPacketLossPercent() {
        return packetLossPercent;
    }

    public void setPacketLossPercent(Double packetLossPercent) {
        this.packetLossPercent = packetLossPercent;
    }

    public Double getMosScore() {
        return mosScore;
    }

    public void setMosScore(Double mosScore) {
        this.mosScore = mosScore;
    }

    public Double getRttAvgMs() {
        return rttAvgMs;
    }

    public void setRttAvgMs(Double rttAvgMs) {
        this.rttAvgMs = rttAvgMs;
    }

    public Double getRttMinMs() {
        return rttMinMs;
    }

    public void setRttMinMs(Double rttMinMs) {
        this.rttMinMs = rttMinMs;
    }

    public Double getRttMaxMs() {
        return rttMaxMs;
    }

    public void setRttMaxMs(Double rttMaxMs) {
        this.rttMaxMs = rttMaxMs;
    }

    public String getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(String protocolType) {
        this.protocolType = protocolType;
    }

    public String getQualityStatus() {
        return qualityStatus;
    }

    public void setQualityStatus(String qualityStatus) {
        this.qualityStatus = qualityStatus;
    }

    public Integer getPacketsSent() {
        return packetsSent;
    }

    public void setPacketsSent(Integer packetsSent) {
        this.packetsSent = packetsSent;
    }

    public Integer getPacketsReceived() {
        return packetsReceived;
    }

    public void setPacketsReceived(Integer packetsReceived) {
        this.packetsReceived = packetsReceived;
    }

    public Integer getPacketsLost() {
        return packetsLost;
    }

    public void setPacketsLost(Integer packetsLost) {
        this.packetsLost = packetsLost;
    }

    public Boolean getDuringActiveCall() {
    return duringActiveCall;
}

    public void setDuringActiveCall(Boolean duringActiveCall) {
        this.duringActiveCall = duringActiveCall;
    }

    @Override
    public String toString() {
        return String.format("QoS[IP=%s, Latency=%.2fms, Jitter=%.2fms, Loss=%.2f%%, MOS=%.2f, Status=%s]",
                targetIp, latencyMs, jitterMs, packetLossPercent, mosScore, qualityStatus);
    }
}