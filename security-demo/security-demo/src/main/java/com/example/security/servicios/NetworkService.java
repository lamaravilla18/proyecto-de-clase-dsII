package com.example.security.servicios;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.security.modelo.Evento;
import com.example.security.modelo.PingRequest;
import com.example.security.modelo.PingResponse;
import com.example.security.modelo.PortScanRequest;
import com.example.security.modelo.PortScanResponse;
import com.example.security.modelo.Usuario;
import com.example.security.repositorio.EventoRepository;
import com.example.security.servicios.PortSecurityAnalyzer;

@Service
public class NetworkService {

    @Autowired
    private EventoRepository networkEventRepository;

    private static final Map<Integer, String> COMMON_PORTS = new HashMap<>();

    static {
        COMMON_PORTS.put(21, "FTP");
        COMMON_PORTS.put(22, "SSH");
        COMMON_PORTS.put(23, "Telnet");
        COMMON_PORTS.put(25, "SMTP");
        COMMON_PORTS.put(53, "DNS");
        COMMON_PORTS.put(80, "HTTP");
        COMMON_PORTS.put(110, "POP3");
        COMMON_PORTS.put(143, "IMAP");
        COMMON_PORTS.put(443, "HTTPS");
        COMMON_PORTS.put(3306, "MySQL");
        COMMON_PORTS.put(8080, "HTTP-Proxy");
    }

    public PingResponse executePing(PingRequest request, Usuario usuario) {
        if (!isValidIPOrDomain(request.getIp())) {
            throw new IllegalArgumentException("IP o dominio invalido");
        }

        PingResponse response = new PingResponse();
        String output = "";
        List<Integer> pingTimes = new ArrayList<>();
        int packetsLost = 0;
        BufferedReader reader = null;

        try {
            String pingCmd;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                pingCmd = "ping -n 4 " + request.getIp();
            } else {
                pingCmd = "ping -c 4 " + request.getIp();
            }

            Process process = Runtime.getRuntime().exec(pingCmd);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            Pattern timePattern = Pattern.compile("time[=<]([0-9]+)ms");

            while ((line = reader.readLine()) != null) {
                output += line + "\n";

                Matcher matcher = timePattern.matcher(line);
                if (matcher.find()) {
                    try {
                        int pingTime = Integer.parseInt(matcher.group(1));
                        pingTimes.add(pingTime);
                    } catch (NumberFormatException e) {
                        // Ignorar errores de conversión
                    }
                }

                if (line.toLowerCase().contains("request timed out") ||
                        line.toLowerCase().contains("destination host unreachable") ||
                        line.toLowerCase().contains("100% packet loss")) {
                    packetsLost++;
                }
            }

            process.waitFor();

            // Registrar evento
            Evento event = new Evento();
            event.setIp(request.getIp());
            Date now = new Date();
            event.setFecha(now);
            event.setHora(now);
            event.setDescripcion("Ping a " + request.getIp());
            event.setUsuario(usuario);
            networkEventRepository.save(event);

        } catch (IOException | InterruptedException e) {
            output = "Error al ejecutar ping: " + e.getMessage();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                // Ignorar errores al cerrar el reader
            }
        }

        response.setOutput(output);
        response.setPingTimes(pingTimes);
        response.setPacketsLost(packetsLost);

        return response;
    }

    public PortScanResponse scanPorts(PortScanRequest request, Usuario usuario) {
        if (!isValidIPOrDomain(request.getIp())) {
            throw new IllegalArgumentException("IP o dominio invalido");
        }

        String[] rangeParts = request.getPortRange().split("-");
        if (rangeParts.length != 2) {
            throw new IllegalArgumentException("Rango de puertos invalido. Use formato '1-1000'");
        }

        int minPort, maxPort;
        try {
            minPort = Integer.parseInt(rangeParts[0]);
            maxPort = Integer.parseInt(rangeParts[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Rango de puertos invalido");
        }

        if (minPort < 1 || maxPort > 65535 || minPort > maxPort) {
            throw new IllegalArgumentException("Rango de puertos fuera de limites (1-65535)");
        }

        if (maxPort - minPort > 1000) {
            maxPort = minPort + 1000;
        }

        List<Map<String, Object>> openPorts = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(20);
        List<Future<Map<String, Object>>> futures = new ArrayList<>();

        for (int port = minPort; port <= maxPort; port++) {
            final int currentPort = port;
            futures.add(executor.submit(() -> {
                Map<String, Object> result = new HashMap<>();
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(request.getIp(), currentPort), 200);
                    result.put("port", currentPort);
                    result.put("status", "abierto");
                    result.put("service", COMMON_PORTS.getOrDefault(currentPort, "desconocido"));
                    return result;
                } catch (Exception e) {
                    return null;
                }
            }));
        }

        for (Future<Map<String, Object>> future : futures) {
            try {
                Map<String, Object> result = future.get(300, TimeUnit.MILLISECONDS);
                if (result != null) {
                    openPorts.add(result);
                }
            } catch (java.util.concurrent.TimeoutException | InterruptedException
                    | java.util.concurrent.ExecutionException e) {
                // Ignorar timeout y otros errores esperados
            }
        }

        executor.shutdown();

        // Registrar evento
        Evento event = new Evento();
        event.setIp(request.getIp());
        Date now = new Date();
        event.setFecha(now);
        event.setHora(now);
        event.setDescripcion("Escaneo de puertos a " + request.getIp() + " (" + request.getPortRange() + ")");
        event.setUsuario(usuario);
        networkEventRepository.save(event);

        // *** NUEVO: Análisis de seguridad de los puertos ***
        PortSecurityAnalyzer.SecurityAnalysis securityAnalysis = 
            PortSecurityAnalyzer.analyzeOpenPorts(openPorts);
        
        PortScanResponse response = new PortScanResponse();
        response.setIp(request.getIp());
        response.setPortRange(request.getPortRange());
        response.setOpenPorts(openPorts);
        response.setTotalScanned(maxPort - minPort + 1);
        response.setSecurityAnalysis(securityAnalysis); // Agregar análisis de seguridad

        return response;
    }

    public List<Evento> getAllEvents() {
        return networkEventRepository.findAll();
    }

    private boolean isValidIPOrDomain(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        return !(input.contains(";") ||
                input.contains("&") ||
                input.contains("|") ||
                input.contains("`") ||
                input.contains("$") ||
                input.contains(">") ||
                input.contains("<") ||
                input.contains("\n"));
    }
}