package com.example.security.dto;

public class QoSData {
    private double latencyMs;
    private double jitterMs;
    private double packetLossPercent;
    private double mosScore;
    private String qualityStatus;
    private int packetsSent;
    private int packetsReceived;
    private int packetsLost;
    
    // Métricas adicionales
    private double rttAvgMs;
    private double rttMinMs;
    private double rttMaxMs;
    
    private Boolean duringActiveCall = false;
    private String targetIp;

    public QoSData() {
    }
    
    
    public QoSData(double latencyMs, double jitterMs, double packetLossPercent, double mosScore) {
        this.latencyMs = latencyMs;
        this.jitterMs = jitterMs;
        this.packetLossPercent = packetLossPercent;
        this.mosScore = mosScore;
        this.qualityStatus = calculateQualityStatus();
    }
    
    private String calculateQualityStatus() {
        if (mosScore >= 4.3) return "EXCELLENT";
        if (mosScore >= 4.0) return "GOOD";
        if (mosScore >= 3.6) return "FAIR";
        if (mosScore >= 3.1) return "POOR";
        return "BAD";
    }

    // Getters y Setters
    public double getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(double latencyMs) {
        this.latencyMs = latencyMs;
    }

    public double getJitterMs() {
        return jitterMs;
    }

    public void setJitterMs(double jitterMs) {
        this.jitterMs = jitterMs;
    }

    public double getPacketLossPercent() {
        return packetLossPercent;
    }

    public void setPacketLossPercent(double packetLossPercent) {
        this.packetLossPercent = packetLossPercent;
    }

    public double getMosScore() {
        return mosScore;
    }

    public void setMosScore(double mosScore) {
        this.mosScore = mosScore;
        this.qualityStatus = calculateQualityStatus();
    }

    public String getQualityStatus() {
        return qualityStatus;
    }

    public void setQualityStatus(String qualityStatus) {
        this.qualityStatus = qualityStatus;
    }

    public int getPacketsSent() {
        return packetsSent;
    }

    public void setPacketsSent(int packetsSent) {
        this.packetsSent = packetsSent;
    }

    public int getPacketsReceived() {
        return packetsReceived;
    }

    public void setPacketsReceived(int packetsReceived) {
        this.packetsReceived = packetsReceived;
    }

    public int getPacketsLost() {
        return packetsLost;
    }

    public void setPacketsLost(int packetsLost) {
        this.packetsLost = packetsLost;
    }

    public double getRttAvgMs() {
        return rttAvgMs;
    }

    public void setRttAvgMs(double rttAvgMs) {
        this.rttAvgMs = rttAvgMs;
    }

    public double getRttMinMs() {
        return rttMinMs;
    }

    public void setRttMinMs(double rttMinMs) {
        this.rttMinMs = rttMinMs;
    }

    public double getRttMaxMs() {
        return rttMaxMs;
    }

    public void setRttMaxMs(double rttMaxMs) {
        this.rttMaxMs = rttMaxMs;
    }


        public Boolean getDuringActiveCall() {
        return duringActiveCall;
    }

    public void setDuringActiveCall(Boolean duringActiveCall) {
        this.duringActiveCall = duringActiveCall;
    }

    public String getTargetIp() {
        return targetIp;
    }

    public void setTargetIp(String targetIp) {
        this.targetIp = targetIp;
    }

    @Override
    public String toString() {
        return String.format("QoS[Latency=%.2fms, Jitter=%.2fms, Loss=%.2f%%, MOS=%.2f, Status=%s]",
                latencyMs, jitterMs, packetLossPercent, mosScore, qualityStatus);
    }
}