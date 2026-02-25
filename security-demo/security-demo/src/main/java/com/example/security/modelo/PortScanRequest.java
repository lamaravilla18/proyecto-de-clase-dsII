package com.example.security.modelo;

public class PortScanRequest {
    private String ip;
    private String portRange;
    
    // Getters y setters
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    
    public String getPortRange() { return portRange; }
    public void setPortRange(String portRange) { this.portRange = portRange; }
}