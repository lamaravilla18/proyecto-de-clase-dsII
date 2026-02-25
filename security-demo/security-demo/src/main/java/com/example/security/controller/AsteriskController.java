package com.example.security.controller;

import com.example.security.servicios.AsteriskAMIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.*;

@RestController
@RequestMapping("/api/voip")
@CrossOrigin(origins = "*")
public class AsteriskController {

    @Autowired
    private AsteriskAMIService amiService;

    /**
     * GET /api/voip/status
     * Verifica el estado del servidor Asterisk
     */
    @GetMapping("/status")
    public ResponseEntity<?> getAsteriskStatus() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Intentar ping al AMI
            boolean connected = amiService.ping();
            
            if (connected) {
                // Obtener versión
                String versionResponse = amiService.getCoreShowVersion();
                
                result.put("status", "online");
                
                // Extraer versión del response
                if (versionResponse.contains("Asterisk")) {
                    Pattern pattern = Pattern.compile("Asterisk.*?\\d+\\.\\d+\\.\\d+");
                    Matcher matcher = pattern.matcher(versionResponse);
                    if (matcher.find()) {
                        result.put("version", matcher.group(0));
                    } else {
                        result.put("version", "Asterisk (conectado)");
                    }
                } else {
                    result.put("version", "Asterisk AMI");
                }
            } else {
                result.put("status", "offline");
                result.put("error", "No se pudo conectar al AMI de Asterisk");
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("status", "offline");
            error.put("error", e.getMessage());
            return ResponseEntity.ok(error);
        }
    }

    /**
     * GET /api/voip/extensions
     * Obtiene todas las extensiones SIP registradas
     */
    @GetMapping("/extensions")
    public ResponseEntity<?> getExtensions() {
        try {
            String response = amiService.getSipPeers();
            List<Map<String, String>> extensions = new ArrayList<>();
            
            System.out.println("=== Parseando extensiones del AMI ===");
            
            // Parsear eventos PeerEntry
            String[] lines = response.split("\n");
            Map<String, String> currentPeer = new HashMap<>();
            
            for (String line : lines) {
                line = line.trim();
                
                if (line.equals("Event: PeerEntry")) {
                    // Nuevo peer, guardar el anterior si existe
                    if (!currentPeer.isEmpty() && currentPeer.containsKey("ObjectName")) {
                        System.out.println("Peer encontrado: " + currentPeer);
                        
                        Map<String, String> ext = new HashMap<>();
                        String objectName = currentPeer.get("ObjectName");
                        
                        // Extraer el número de extensión del CallerID o usar ObjectName
                        String extensionNumber = extractExtensionNumber(currentPeer.getOrDefault("CallerID", objectName));
                        
                        ext.put("extension", extensionNumber);
                        ext.put("name", objectName);
                        
                        String ip = currentPeer.getOrDefault("IPaddress", "N/A");
                        String port = currentPeer.getOrDefault("IPport", "");
                        ext.put("host", port.isEmpty() ? ip : ip + ":" + port);
                        
                        // El status "Unmonitored" significa que está registrado
                        String status = currentPeer.getOrDefault("Status", "UNREACHABLE");
                        if (status.equals("Unmonitored") || status.contains("OK")) {
                            ext.put("status", "OK");
                        } else {
                            ext.put("status", "UNREACHABLE");
                        }
                        
                        extensions.add(ext);
                    }
                    currentPeer.clear();
                    
                } else if (line.startsWith("ObjectName:")) {
                    currentPeer.put("ObjectName", line.substring(11).trim());
                    
                } else if (line.startsWith("IPaddress:")) {
                    currentPeer.put("IPaddress", line.substring(10).trim());
                    
                } else if (line.startsWith("IPport:")) {
                    currentPeer.put("IPport", line.substring(7).trim());
                    
                } else if (line.startsWith("Status:")) {
                    String statusLine = line.substring(7).trim();
                    currentPeer.put("Status", statusLine);
                    
                } else if (line.startsWith("CallerID:")) {
                    currentPeer.put("CallerID", line.substring(9).trim());
                    
                } else if (line.contains("PeerlistComplete") || line.contains("EventList: Complete")) {
                    // Guardar el último peer
                    if (!currentPeer.isEmpty() && currentPeer.containsKey("ObjectName")) {
                        System.out.println("Último peer encontrado: " + currentPeer);
                        
                        Map<String, String> ext = new HashMap<>();
                        String objectName = currentPeer.get("ObjectName");
                        
                        // Extraer el número de extensión del CallerID o usar ObjectName
                        String extensionNumber = extractExtensionNumber(currentPeer.getOrDefault("CallerID", objectName));
                        
                        ext.put("extension", extensionNumber);
                        ext.put("name", objectName);
                        
                        String ip = currentPeer.getOrDefault("IPaddress", "N/A");
                        String port = currentPeer.getOrDefault("IPport", "");
                        ext.put("host", port.isEmpty() ? ip : ip + ":" + port);
                        
                        String status = currentPeer.getOrDefault("Status", "UNREACHABLE");
                        if (status.equals("Unmonitored") || status.contains("OK")) {
                            ext.put("status", "OK");
                        } else {
                            ext.put("status", "UNREACHABLE");
                        }
                        
                        extensions.add(ext);
                    }
                    break;
                }
            }
            
            System.out.println("Total extensiones encontradas: " + extensions.size());
            System.out.println("Extensiones: " + extensions);
            
            return ResponseEntity.ok(extensions);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Extrae el número de extensión del CallerID
     * Entrada: "Vendedor <1002>" o "1002" o "venta"
     * Salida: "1002" o el valor original si no encuentra número
     */
    private String extractExtensionNumber(String callerIdOrName) {
        if (callerIdOrName == null || callerIdOrName.isEmpty()) {
            return "N/A";
        }
        
        // Buscar número entre < >
        Pattern pattern = Pattern.compile("<(\\d+)>");
        Matcher matcher = pattern.matcher(callerIdOrName);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // Buscar cualquier secuencia de dígitos
        pattern = Pattern.compile("(\\d+)");
        matcher = pattern.matcher(callerIdOrName);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // Si no hay números, devolver el original
        return callerIdOrName;
    }

    /**
     * GET /api/voip/active-calls
     * Obtiene las llamadas activas en el sistema
     */
    @GetMapping("/active-calls")
    public ResponseEntity<?> getActiveCalls() {
        try {
            String response = amiService.getCoreShowChannels();
            List<Map<String, Object>> calls = new ArrayList<>();
            int activeChannels = 0;
            
            // Parsear respuesta
            String[] lines = response.split("\n");
            Map<String, String> currentChannel = new HashMap<>();
            
            for (String line : lines) {
                line = line.trim();
                
                if (line.equals("Event: CoreShowChannel")) {
                    // Nuevo canal
                    if (!currentChannel.isEmpty()) {
                        Map<String, Object> call = new HashMap<>();
                        call.put("channel", currentChannel.getOrDefault("Channel", "Unknown"));
                        call.put("state", currentChannel.getOrDefault("ChannelStateDesc", "Unknown"));
                        call.put("application", currentChannel.getOrDefault("Application", "Unknown"));
                        calls.add(call);
                    }
                    currentChannel.clear();
                } else if (line.startsWith("Channel:")) {
                    currentChannel.put("Channel", line.substring(8).trim());
                } else if (line.startsWith("ChannelStateDesc:")) {
                    currentChannel.put("ChannelStateDesc", line.substring(17).trim());
                } else if (line.startsWith("Application:")) {
                    currentChannel.put("Application", line.substring(12).trim());
                } else if (line.startsWith("ListItems:")) {
                    String items = line.substring(10).trim();
                    try {
                        activeChannels = Integer.parseInt(items);
                    } catch (NumberFormatException e) {
                        activeChannels = 0;
                    }
                }
            }
            
            // Agregar último canal si existe
            if (!currentChannel.isEmpty()) {
                Map<String, Object> call = new HashMap<>();
                call.put("channel", currentChannel.getOrDefault("Channel", "Unknown"));
                call.put("state", currentChannel.getOrDefault("ChannelStateDesc", "Unknown"));
                call.put("application", currentChannel.getOrDefault("Application", "Unknown"));
                calls.add(call);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("calls", calls);
            result.put("count", activeChannels);
            result.put("timestamp", new Date());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/voip/call-history
     * Obtiene el historial reciente de llamadas (CDR)
     */
    @GetMapping("/call-history")
    public ResponseEntity<?> getCallHistory() {
        try {
            // Primero obtener el mapa de extensiones
            Map<String, String> extensionNames = getExtensionNamesMap();
            
            // Leer CDR con wsl
            ProcessBuilder pb = new ProcessBuilder(
                "wsl", "tail", "-n", "20", "/var/log/asterisk/cdr-csv/Master.csv"
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            List<Map<String, Object>> history = new ArrayList<>();
            String line;
            
            while ((line = reader.readLine()) != null) {
                String[] fields = line.replace("\"", "").split(",");
                if (fields.length >= 14) {
                    Map<String, Object> call = new HashMap<>();
                    
                    String src = fields[1].trim();
                    String dst = fields[2].trim();
                    
                    // Agregar nombres de extensiones si existen
                    String srcDisplay = src;
                    String dstDisplay = dst;
                    
                    if (extensionNames.containsKey(src)) {
                        srcDisplay = src + " (" + extensionNames.get(src) + ")";
                    }
                    if (extensionNames.containsKey(dst)) {
                        dstDisplay = dst + " (" + extensionNames.get(dst) + ")";
                    }
                    
                    call.put("src", srcDisplay);
                    call.put("dst", dstDisplay);
                    
                    // Parsear y formatear fecha/hora
                    String startTime = fields[9].trim();
                    call.put("start", formatCallDateTime(startTime));
                    
                    call.put("duration", fields.length > 12 ? fields[12] : "0");
                    call.put("billsec", fields.length > 13 ? fields[13] : "0");
                    
                    // Campos adicionales por si los necesitas después
                    call.put("accountcode", fields[0]);
                    call.put("dcontext", fields[3]);
                    call.put("clid", fields[4]);
                    call.put("channel", fields[5]);
                    call.put("dstchannel", fields[6]);
                    call.put("lastapp", fields[7]);
                    call.put("lastdata", fields[8]);
                    call.put("answer", fields.length > 10 ? fields[10] : "");
                    call.put("end", fields.length > 11 ? fields[11] : "");
                    call.put("disposition", fields.length > 14 ? fields[14] : "UNKNOWN");
                    
                    history.add(call);
                }
            }
            reader.close();
            process.waitFor();
            
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            System.err.println("Error leyendo CDR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    /**
     * Obtiene un mapa de extensión -> nombre
     */
    private Map<String, String> getExtensionNamesMap() {
        Map<String, String> map = new HashMap<>();
        try {
            String response = amiService.getSipPeers();
            String[] lines = response.split("\n");
            String currentExtension = null;
            
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("ObjectName:")) {
                    currentExtension = line.substring(11).trim();
                    // Por ahora, nombre = extensión (puedes extenderlo después)
                    map.put(currentExtension, currentExtension);
                }
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo nombres de extensiones: " + e.getMessage());
        }
        return map;
    }
    
    /**
     * Formatea la fecha/hora del CDR a formato legible
     * Entrada: "2025-10-26 11:30:45" (formato CDR de Asterisk)
     * Salida: "26/10/2025 11:30:45"
     */
    private String formatCallDateTime(String dateTime) {
        try {
            if (dateTime == null || dateTime.isEmpty()) {
                return "-";
            }
            
            // El formato del CDR suele ser: YYYY-MM-DD HH:mm:ss
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            
            java.util.Date date = inputFormat.parse(dateTime);
            return outputFormat.format(date);
        } catch (Exception e) {
            // Si no se puede parsear, devolver el original
            return dateTime;
        }
    }

    /**
     * GET /api/voip/statistics
     * Obtiene estadísticas generales del sistema VoIP
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Obtener extensiones
            String peersResponse = amiService.getSipPeers();
            int totalExtensions = 0;
            int onlineExtensions = 0;
            
            String[] lines = peersResponse.split("\n");
            Map<String, String> currentPeer = new HashMap<>();
            
            for (String line : lines) {
                line = line.trim();
                
                if (line.equals("Event: PeerEntry")) {
                    if (!currentPeer.isEmpty() && currentPeer.containsKey("ObjectName")) {
                        totalExtensions++;
                        String status = currentPeer.getOrDefault("Status", "UNREACHABLE");
                        if (status.equals("Unmonitored") || status.contains("OK")) {
                            onlineExtensions++;
                        }
                    }
                    currentPeer.clear();
                } else if (line.startsWith("ObjectName:")) {
                    currentPeer.put("ObjectName", line.substring(11).trim());
                } else if (line.startsWith("Status:")) {
                    currentPeer.put("Status", line.substring(7).trim());
                } else if (line.contains("PeerlistComplete")) {
                    if (!currentPeer.isEmpty() && currentPeer.containsKey("ObjectName")) {
                        totalExtensions++;
                        String status = currentPeer.getOrDefault("Status", "UNREACHABLE");
                        if (status.equals("Unmonitored") || status.contains("OK")) {
                            onlineExtensions++;
                        }
                    }
                    break;
                }
            }
            
            // Obtener llamadas activas
            String channelsResponse = amiService.getCoreShowChannels();
            int activeCalls = 0;
            
            Pattern pattern = Pattern.compile("ListItems:\\s*(\\d+)");
            Matcher matcher = pattern.matcher(channelsResponse);
            if (matcher.find()) {
                activeCalls = Integer.parseInt(matcher.group(1));
            }
            
            stats.put("totalExtensions", totalExtensions);
            stats.put("onlineExtensions", onlineExtensions);
            stats.put("offlineExtensions", totalExtensions - onlineExtensions);
            stats.put("activeCalls", activeCalls);
            stats.put("timestamp", new Date());
            
            System.out.println("Estadísticas: " + stats);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/voip/test-connection
     * Endpoint de prueba para verificar conexión AMI
     */
    @GetMapping("/test-connection")
    public ResponseEntity<?> testConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean connected = amiService.connect();
            result.put("connected", connected);
            result.put("message", connected ? "Conexión exitosa al AMI" : "Error de conexión");
            
            if (connected) {
                String peers = amiService.getSipPeers();
                result.put("peersResponse", peers);
                
                String channels = amiService.getCoreShowChannels();
                result.put("channelsResponse", channels);
            }
            
        } catch (Exception e) {
            result.put("connected", false);
            result.put("error", e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(result);
    }
}