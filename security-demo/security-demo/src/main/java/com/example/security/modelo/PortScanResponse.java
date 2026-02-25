package com.example.security.modelo;

import java.util.List;
import java.util.Map;
import com.example.security.servicios.PortSecurityAnalyzer;

public class PortScanResponse {
    private String ip;
    private String portRange;
    private List<Map<String, Object>> openPorts;
    private int totalScanned;
    private PortSecurityAnalyzer.SecurityAnalysis securityAnalysis; // NUEVO
    
    // Getters y setters
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    
    public String getPortRange() { return portRange; }
    public void setPortRange(String portRange) { this.portRange = portRange; }
    
    public List<Map<String, Object>> getOpenPorts() { return openPorts; }
    public void setOpenPorts(List<Map<String, Object>> openPorts) { this.openPorts = openPorts; }
    
    public int getTotalScanned() { return totalScanned; }
    public void setTotalScanned(int totalScanned) { this.totalScanned = totalScanned; }
    
    // NUEVO: Getter y setter para análisis de seguridad
    public PortSecurityAnalyzer.SecurityAnalysis getSecurityAnalysis() { 
        return securityAnalysis; 
    }
    public void setSecurityAnalysis(PortSecurityAnalyzer.SecurityAnalysis securityAnalysis) { 
        this.securityAnalysis = securityAnalysis; 
    }
}