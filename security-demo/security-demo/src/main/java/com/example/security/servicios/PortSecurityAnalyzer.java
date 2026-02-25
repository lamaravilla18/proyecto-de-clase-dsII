package com.example.security.servicios;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortSecurityAnalyzer {
    
    // Base de datos de puertos con sus riesgos
    private static final Map<Integer, PortRisk> PORT_RISKS = new HashMap<>();
    
    static {
        // Puertos de alto riesgo
        PORT_RISKS.put(21, new PortRisk("FTP", "ALTO", 
            "Transmite credenciales en texto plano.\nPosible interceptación de datos.",
            "Usar SFTP (puerto 22) o FTPS en su lugar."));
        
        PORT_RISKS.put(23, new PortRisk("Telnet", "CRÍTICO", 
            "Transmite TODO en texto plano incluyendo contraseñas.\nAltamente vulnerable.",
            "Usar SSH (puerto 22) en su lugar.\nDeshabilitar Telnet inmediatamente."));
        
        PORT_RISKS.put(25, new PortRisk("SMTP", "MEDIO", 
            "Puede ser usado para envío de spam o relay no autorizado.",
            "Configurar autenticación SMTP y usar TLS.\nRestringir acceso externo."));
        
        PORT_RISKS.put(53, new PortRisk("DNS", "MEDIO", 
            "Vulnerable a ataques de amplificación DDoS y\nenvenenamiento de caché.",
            "Limitar consultas recursivas.\nActualizar servidor DNS regularmente."));
        
        PORT_RISKS.put(80, new PortRisk("HTTP", "MEDIO", 
            "Tráfico sin cifrar.\nDatos y credenciales expuestos.",
            "Migrar a HTTPS (puerto 443).\nRedirigir todo el tráfico HTTP a HTTPS."));
        
        PORT_RISKS.put(110, new PortRisk("POP3", "ALTO", 
            "Transmite credenciales de correo en texto plano.",
            "Usar POP3S (puerto 995) o IMAP con TLS."));
        
        PORT_RISKS.put(143, new PortRisk("IMAP", "ALTO", 
            "Transmite credenciales de correo en texto plano.",
            "Usar IMAPS (puerto 993) con cifrado TLS."));
        
        PORT_RISKS.put(445, new PortRisk("SMB", "CRÍTICO", 
            "Altamente vulnerable a ransomware\n(WannaCry, EternalBlue).",
            "Bloquear acceso externo.\nAplicar parches de seguridad.\nUsar VPN para acceso remoto."));
        
        PORT_RISKS.put(1433, new PortRisk("MS SQL Server", "ALTO", 
            "Base de datos expuesta.\nPosible extracción de información sensible.",
            "Bloquear acceso externo.\nUsar firewall y autenticación fuerte.\nCambiar puerto predeterminado."));
        
        PORT_RISKS.put(3306, new PortRisk("MySQL", "ALTO", 
            "Base de datos expuesta.\nRiesgo de inyección SQL y robo de datos.",
            "Bloquear acceso externo.\nPermitir solo conexiones locales o a través de VPN."));
        
        PORT_RISKS.put(3389, new PortRisk("RDP (Remote Desktop)", "CRÍTICO", 
            "Objetivo principal de ataques de fuerza bruta\ny ransomware.",
            "Deshabilitar si no es necesario.\nUsar VPN.\nImplementar autenticación de dos factores."));
        
        PORT_RISKS.put(5432, new PortRisk("PostgreSQL", "ALTO", 
            "Base de datos expuesta.\nRiesgo de acceso no autorizado.",
            "Bloquear acceso externo.\nConfigurar pg_hba.conf correctamente."));
        
        PORT_RISKS.put(5900, new PortRisk("VNC", "ALTO", 
            "Acceso remoto con cifrado débil o nulo.",
            "Usar VPN para acceso remoto.\nImplementar contraseñas fuertes."));
        
        PORT_RISKS.put(8080, new PortRisk("HTTP-Proxy/Tomcat", "MEDIO", 
            "Panel de administración potencialmente expuesto\nsin cifrado.",
            "Usar HTTPS.\nRestringir acceso con firewall.\nCambiar credenciales predeterminadas."));
        
        PORT_RISKS.put(27017, new PortRisk("MongoDB", "CRÍTICO", 
            "Base de datos NoSQL frecuentemente mal configurada\ny expuesta.",
            "Habilitar autenticación.\nBloquear acceso externo completamente."));
        
        PORT_RISKS.put(6379, new PortRisk("Redis", "ALTO", 
            "Cache/DB sin autenticación por defecto.",
            "Habilitar autenticación.\nNunca exponer a Internet."));
        
        PORT_RISKS.put(9200, new PortRisk("Elasticsearch", "CRÍTICO", 
            "Puede exponer datos sensibles sin autenticación.",
            "Habilitar seguridad X-Pack.\nBloquear acceso público."));
    }
    
    public static class PortRisk {
        private String serviceName;
        private String riskLevel; // BAJO, MEDIO, ALTO, CRÍTICO
        private String vulnerability;
        private String recommendation;
        
        public PortRisk(String serviceName, String riskLevel, String vulnerability, String recommendation) {
            this.serviceName = serviceName;
            this.riskLevel = riskLevel;
            this.vulnerability = vulnerability;
            this.recommendation = recommendation;
        }
        
        // Getters
        public String getServiceName() { return serviceName; }
        public String getRiskLevel() { return riskLevel; }
        public String getVulnerability() { return vulnerability; }
        public String getRecommendation() { return recommendation; }
    }
    
    public static class SecurityAnalysis {
        private int totalOpenPorts;
        private int criticalPorts;
        private int highRiskPorts;
        private int mediumRiskPorts;
        private int lowRiskPorts;
        private List<PortSecurityInfo> portDetails;
        private String overallRisk;
        
        public SecurityAnalysis() {
            this.portDetails = new ArrayList<>();
        }
        
        // Getters y setters
        public int getTotalOpenPorts() { return totalOpenPorts; }
        public void setTotalOpenPorts(int totalOpenPorts) { this.totalOpenPorts = totalOpenPorts; }
        
        public int getCriticalPorts() { return criticalPorts; }
        public void setCriticalPorts(int criticalPorts) { this.criticalPorts = criticalPorts; }
        
        public int getHighRiskPorts() { return highRiskPorts; }
        public void setHighRiskPorts(int highRiskPorts) { this.highRiskPorts = highRiskPorts; }
        
        public int getMediumRiskPorts() { return mediumRiskPorts; }
        public void setMediumRiskPorts(int mediumRiskPorts) { this.mediumRiskPorts = mediumRiskPorts; }
        
        public int getLowRiskPorts() { return lowRiskPorts; }
        public void setLowRiskPorts(int lowRiskPorts) { this.lowRiskPorts = lowRiskPorts; }
        
        public List<PortSecurityInfo> getPortDetails() { return portDetails; }
        public void setPortDetails(List<PortSecurityInfo> portDetails) { this.portDetails = portDetails; }
        
        public String getOverallRisk() { return overallRisk; }
        public void setOverallRisk(String overallRisk) { this.overallRisk = overallRisk; }
    }
    
    public static class PortSecurityInfo {
        private int port;
        private String service;
        private String riskLevel;
        private String vulnerability;
        private String recommendation;
        
        public PortSecurityInfo(int port, String service, String riskLevel, 
                               String vulnerability, String recommendation) {
            this.port = port;
            this.service = service;
            this.riskLevel = riskLevel;
            this.vulnerability = vulnerability;
            this.recommendation = recommendation;
        }
        
        // Getters
        public int getPort() { return port; }
        public String getService() { return service; }
        public String getRiskLevel() { return riskLevel; }
        public String getVulnerability() { return vulnerability; }
        public String getRecommendation() { return recommendation; }
    }
    
    /**
     * Analiza la seguridad de los puertos abiertos
     */
    public static SecurityAnalysis analyzeOpenPorts(List<Map<String, Object>> openPorts) {
        SecurityAnalysis analysis = new SecurityAnalysis();
        analysis.setTotalOpenPorts(openPorts.size());
        
        int critical = 0, high = 0, medium = 0, low = 0;
        
        for (Map<String, Object> portInfo : openPorts) {
            int port = (int) portInfo.get("port");
            String service = (String) portInfo.get("service");
            
            PortRisk risk = PORT_RISKS.get(port);
            
            if (risk != null) {
                // Puerto conocido con riesgos
                PortSecurityInfo secInfo = new PortSecurityInfo(
                    port,
                    risk.getServiceName(),
                    risk.getRiskLevel(),
                    risk.getVulnerability(),
                    risk.getRecommendation()
                );
                analysis.getPortDetails().add(secInfo);
                
                switch (risk.getRiskLevel()) {
                    case "CRÍTICO": critical++; break;
                    case "ALTO": high++; break;
                    case "MEDIO": medium++; break;
                    default: low++; break;
                }
            } else {
                // Puerto abierto sin información específica de riesgo
                PortSecurityInfo secInfo = new PortSecurityInfo(
                    port,
                    service,
                    "BAJO",
                    "Puerto abierto sin vulnerabilidades conocidas críticas.",
                    "Verificar si el servicio es necesario. Cerrar si no se utiliza."
                );
                analysis.getPortDetails().add(secInfo);
                low++;
            }
        }
        
        analysis.setCriticalPorts(critical);
        analysis.setHighRiskPorts(high);
        analysis.setMediumRiskPorts(medium);
        analysis.setLowRiskPorts(low);
        
        // Determinar riesgo general
        if (critical > 0) {
            analysis.setOverallRisk("CRÍTICO");
        } else if (high > 0) {
            analysis.setOverallRisk("ALTO");
        } else if (medium > 0) {
            analysis.setOverallRisk("MEDIO");
        } else {
            analysis.setOverallRisk("BAJO");
        }
        
        return analysis;
    }
}