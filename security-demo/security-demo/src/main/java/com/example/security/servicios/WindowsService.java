package com.example.security.servicios;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class WindowsService {
    
    private static final Logger logger = LoggerFactory.getLogger(WindowsService.class);
    
    private final String serverIp; // IP de tu Windows Server
    private final String username;
    private final String password;
    
    public WindowsService() {
        // 👈 CONFIGURA AQUÍ TU WINDOWS SERVER
        this.serverIp = "192.168.1.3"; // Cambia por tu IP
        this.username = "Administrator";
        this.password = "Admin123";
    }
    
    /**
     * Verificar si el servidor está alcanzable
     */
    public Map<String, Object> checkServerConnectivity() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Ping al servidor
            ProcessBuilder pb = new ProcessBuilder("ping", "-c", "4", serverIp);
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            int exitCode = process.waitFor();
            
            result.put("status", exitCode == 0 ? "ONLINE" : "OFFLINE");
            result.put("serverIp", serverIp);
            result.put("pingOutput", output.toString());
            
        } catch (Exception e) {
            logger.error("Error al verificar conectividad con Windows Server: {}", e.getMessage());
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Obtener estado de servicios de Windows (requiere WMI o PowerShell remoto)
     * Para simplificar, haremos monitoreo básico
     */
    public Map<String, Object> getWindowsServices() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, String>> services = new ArrayList<>();
        
        // Lista de servicios importantes a monitorear
        String[] servicesToCheck = {
            "W3SVC",        // IIS
            "MSSQLSERVER",  // SQL Server
            "Spooler",      // Print Spooler
            "Dnscache",     // DNS Client
            "NTDS"          // Active Directory (si aplica)
        };
        
        result.put("serverIp", serverIp);
        result.put("services", services);
        result.put("note", "Para monitoreo completo, habilitar WinRM en Windows Server");
        
        return result;
    }
    
    /**
     * Monitorear recursos del servidor (requiere acceso remoto)
     */
    public Map<String, Object> getServerResources() {
        Map<String, Object> result = new HashMap<>();
        
        result.put("serverIp", serverIp);
        result.put("cpuUsage", "N/A - Requiere WinRM");
        result.put("ramUsage", "N/A - Requiere WinRM");
        result.put("diskUsage", "N/A - Requiere WinRM");
        
        return result;
    }
}