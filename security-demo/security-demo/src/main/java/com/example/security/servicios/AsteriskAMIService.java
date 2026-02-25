package com.example.security.servicios;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.io.*;
import java.net.Socket;
import java.util.*;

@Service
public class AsteriskAMIService {

    @Value("${asterisk.host}")
    private String asteriskHost;

    @Value("${asterisk.port}")
    private int asteriskPort;

    @Value("${asterisk.username}")
    private String asteriskUsername;

    @Value("${asterisk.password}")
    private String asteriskPassword;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean isConnected = false;

    /**
     * Conecta al AMI de Asterisk
     */
    public synchronized boolean connect() {
        if (isConnected && socket != null && !socket.isClosed()) {
            return true; // Ya está conectado
        }
        
        try {
            System.out.println("=== Conectando a Asterisk AMI ===");
            System.out.println("Host: " + asteriskHost + ":" + asteriskPort);
            
            socket = new Socket(asteriskHost, asteriskPort);
            socket.setSoTimeout(5000); // Timeout de 5 segundos
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            
            // Leer banner de bienvenida
            String banner = reader.readLine();
            System.out.println("Banner: " + banner);
            
            // Hacer login
            writer.println("Action: Login");
            writer.println("Username: " + asteriskUsername);
            writer.println("Secret: " + asteriskPassword);
            writer.println("Events: off");
            writer.println("");
            
            // Leer respuesta de login
            String response = readSimpleResponse();
            System.out.println("Login response:\n" + response);
            
            isConnected = response.contains("Success");
            
            if (isConnected) {
                System.out.println("✓ Conexión exitosa a Asterisk AMI");
            } else {
                System.out.println("✗ Error en login AMI");
                disconnect();
            }
            
            return isConnected;
        } catch (Exception e) {
            System.err.println("Error al conectar con AMI: " + e.getMessage());
            e.printStackTrace();
            isConnected = false;
            disconnect();
            return false;
        }
    }

    /**
     * Lee una respuesta simple (sin eventos múltiples)
     */
    private String readSimpleResponse() throws IOException {
        StringBuilder response = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null) {
            response.append(line).append("\n");
            if (line.trim().isEmpty()) {
                break; // Línea vacía indica fin de respuesta
            }
        }
        
        return response.toString();
    }

    /**
     * Lee una respuesta completa con múltiples eventos
     * Se detiene cuando encuentra el evento "Complete"
     */
    private String readCompleteEventList() throws IOException {
        StringBuilder response = new StringBuilder();
        String line;
        boolean inEvent = false;
        
        while ((line = reader.readLine()) != null) {
            response.append(line).append("\n");
            
            // Detectar fin de lista de eventos
            if (line.contains("EventList: Complete") || 
                line.contains("PeerlistComplete") ||
                line.contains("CoreShowChannelsComplete")) {
                
                // Leer hasta la línea vacía
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                    if (line.trim().isEmpty()) {
                        return response.toString();
                    }
                }
                break;
            }
            
            // Si encontramos un error o respuesta fallida, terminar
            if (line.contains("Response: Error") || line.contains("Message: Command not found")) {
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                    if (line.trim().isEmpty()) {
                        break;
                    }
                }
                return response.toString();
            }
        }
        
        return response.toString();
    }

    /**
     * Envía un comando AMI y obtiene respuesta
     */
    public synchronized String sendCommand(String action, Map<String, String> parameters) {
        try {
            if (!isConnected) {
                if (!connect()) {
                    return "Error: No conectado al AMI";
                }
            }
            
            System.out.println(">>> Enviando acción: " + action);
            
            writer.println("Action: " + action);
            if (parameters != null) {
                for (Map.Entry<String, String> entry : parameters.entrySet()) {
                    writer.println(entry.getKey() + ": " + entry.getValue());
                }
            }
            writer.println("");
            writer.flush();
            
            // Si es un comando que devuelve lista de eventos, usar readCompleteEventList
            if (action.equals("SIPpeers") || action.equals("CoreShowChannels")) {
                String response = readCompleteEventList();
                System.out.println("<<< Respuesta completa recibida (" + response.length() + " caracteres)");
                return response;
            } else {
                String response = readSimpleResponse();
                return response;
            }
            
        } catch (Exception e) {
            System.err.println("Error al enviar comando AMI: " + e.getMessage());
            e.printStackTrace();
            isConnected = false;
            disconnect();
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Obtiene la versión de Asterisk
     */
    public String getCoreShowVersion() {
        String response = sendCommand("Command", Map.of("Command", "core show version"));
        return response;
    }

    /**
     * Obtiene los peers SIP
     */
    public String getSipPeers() {
        System.out.println("=== Solicitando SIP Peers ===");
        String response = sendCommand("SIPpeers", null);
        System.out.println("=== SIP Peers Response ===");
        System.out.println(response);
        return response;
    }

    /**
     * Obtiene canales activos
     */
    public String getCoreShowChannels() {
        System.out.println("=== Solicitando Core Show Channels ===");
        String response = sendCommand("CoreShowChannels", null);
        System.out.println("=== Core Show Channels Response ===");
        System.out.println(response);
        return response;
    }

    /**
     * Hace ping al servidor
     */
    public boolean ping() {
        try {
            if (!isConnected) {
                return connect();
            }
            
            String response = sendCommand("Ping", null);
            return response.contains("Pong");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Desconecta del AMI
     */
    @PreDestroy
    public synchronized void disconnect() {
        try {
            if (writer != null && isConnected) {
                writer.println("Action: Logoff");
                writer.println("");
                writer.flush();
            }
            
            if (reader != null) {
                try { reader.close(); } catch (Exception e) {}
            }
            if (writer != null) {
                try { writer.close(); } catch (Exception e) {}
            }
            if (socket != null && !socket.isClosed()) {
                try { socket.close(); } catch (Exception e) {}
            }
            
            isConnected = false;
            System.out.println("✓ Desconectado de Asterisk AMI");
        } catch (Exception e) {
            System.err.println("Error al desconectar: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return isConnected && socket != null && !socket.isClosed();
    }
}