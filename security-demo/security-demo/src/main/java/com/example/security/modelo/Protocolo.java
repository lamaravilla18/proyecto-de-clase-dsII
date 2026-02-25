package com.example.security.modelo;

public class Protocolo {
    private double httpMbps;
    private double voipMbps;
    private double streamingMbps;
    private double otherMbps;
    
    public Protocolo() {
        this.httpMbps = 0.0;
        this.voipMbps = 0.0;
        this.streamingMbps = 0.0;
        this.otherMbps = 0.0;
    }
    
    // Getters y Setters
    public double getHttpMbps() {
        return httpMbps;
    }
    
    public void setHttpMbps(double httpMbps) {
        this.httpMbps = httpMbps;
    }
    
    public double getVoipMbps() {
        return voipMbps;
    }
    
    public void setVoipMbps(double voipMbps) {
        this.voipMbps = voipMbps;
    }
    
    public double getStreamingMbps() {
        return streamingMbps;
    }
    
    public void setStreamingMbps(double streamingMbps) {
        this.streamingMbps = streamingMbps;
    }
    
    public double getOtherMbps() {
        return otherMbps;
    }
    
    public void setOtherMbps(double otherMbps) {
        this.otherMbps = otherMbps;
    }
    
    @Override
    public String toString() {
        return String.format("HTTP: %.2f MB/s, VoIP: %.2f MB/s, Streaming: %.2f MB/s, Otros: %.2f MB/s",
                httpMbps, voipMbps, streamingMbps, otherMbps);
    }
}