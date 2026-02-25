package com.example.security.servicios;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.security.modelo.Protocolo;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class NetworkTrafficService {

    private static final Logger logger = LoggerFactory.getLogger(NetworkTrafficService.class);
    
    private final AtomicLong totalBytes = new AtomicLong(0);
    private final AtomicBoolean isCapturing = new AtomicBoolean(false);
    private final List<PcapHandle> activeHandles = new CopyOnWriteArrayList<>();
    private Thread captureThread;
    
    // Variables para calcular velocidad (MB/s)
    private long lastBytes = 0;
    private long lastTime = System.currentTimeMillis();
    private double currentSpeed = 0.0;
    
    // Variables para estadísticas por protocolo
    private final AtomicLong httpBytes = new AtomicLong(0);
    private final AtomicLong voipBytes = new AtomicLong(0);
    private final AtomicLong streamingBytes = new AtomicLong(0);
    private final AtomicLong otherBytes = new AtomicLong(0);
    
    private long lastHttpBytes = 0;
    private long lastVoipBytes = 0;
    private long lastStreamingBytes = 0;
    private long lastOtherBytes = 0;
    
    private Protocolo currentProtocolStats = new Protocolo();
    
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void printInterfaces() throws PcapNativeException {
        List<PcapNetworkInterface> interfaces = Pcaps.findAllDevs();
        for (int i = 0; i < interfaces.size(); i++) {
            System.out.println(i + ": " + interfaces.get(i).getName() + " - " + interfaces.get(i).getDescription());
        }
    }

    public void startCapturing(Consumer<Object> onUpdate) {
        if (isCapturing.compareAndSet(false, true)) {
            resetCounters();
            
            captureThread = new Thread(() -> {
                try {
                    List<PcapNetworkInterface> interfaces = Pcaps.findAllDevs();
                    
                    System.out.println("Interfaces disponibles:");
                    for (int i = 0; i < interfaces.size(); i++) {
                        PcapNetworkInterface nif = interfaces.get(i);
                        System.out.println(i + ": " + nif.getName() + " - " + nif.getDescription());
                    }
                    
                    // Capturar de Wi-Fi (índice 4) y Loopback (índice 7)
                    List<Integer> interfacesToCapture = List.of(4, 7);
                    
                    for (Integer index : interfacesToCapture) {
                        PcapNetworkInterface nif = interfaces.get(index);
                        System.out.println("Iniciando captura en: " + nif.getName() + " - " + nif.getDescription());
                        
                        PcapHandle handle = nif.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);
                        activeHandles.add(handle);
                        
                        // Crear un thread separado para cada interfaz
                        Thread captureInterfaceThread = new Thread(() -> {
                            try {
                                handle.loop(-1, new PacketListener() {
                                    @Override
                                    public void gotPacket(Packet packet) {
                                        if (!isCapturing.get()) {
                                            try {
                                                handle.breakLoop();
                                            } catch (NotOpenException e) {
                                                logger.error("Error al intentar detener la captura: {}", e.getMessage());
                                            }
                                            return;
                                        }
                                        
                                        int packetSize = packet.length();
                                        long newTotal = totalBytes.addAndGet(packetSize);
                                        
                                        // Clasificar el paquete por protocolo
                                        classifyPacket(packet, packetSize);
                                        
                                        // Calcular velocidad cada segundo
                                        long currentTime = System.currentTimeMillis();
                                        long timeDiff = currentTime - lastTime;
                                        
                                        if (timeDiff >= 1000) { // Cada 1 segundo
                                            long bytesDiff = newTotal - lastBytes;
                                            currentSpeed = (bytesDiff / (1024.0 * 1024.0)) / (timeDiff / 1000.0);
                                            
                                            // Calcular velocidades por protocolo
                                            calculateProtocolSpeeds(timeDiff);
                                            
                                            lastBytes = newTotal;
                                            lastTime = currentTime;
                                            
                                            // Enviar datos por SSE
                                            broadcastTrafficData();
                                            
                                            System.out.println("Velocidad total: " + String.format("%.2f", currentSpeed) + " MB/s");
                                            System.out.println(currentProtocolStats.toString());
                                        }
                                    }
                                });
                            } catch (PcapNativeException | NotOpenException e) {
                                logger.error("Error en captura de interfaz: {}", e.getMessage());
                            } catch (InterruptedException e) {
                                logger.info("Captura interrumpida en interfaz: {}", nif.getName());
                            }
                        });
                        
                        captureInterfaceThread.setDaemon(true);
                        captureInterfaceThread.start();
                    }
                    
                } catch (PcapNativeException e) {
                    logger.error("Error nativo Pcap: {}", e.getMessage(), e);
                } catch (Exception e) {
                    logger.error("Error inesperado: {}", e.getMessage(), e);
                }
            });
            
            captureThread.setDaemon(true);
            captureThread.start();
        }
    }
    
    /**
     * Clasifica paquetes por protocolo usando una estrategia híbrida:
     * - Análisis de puertos
     * - Heurísticas basadas en características del tráfico
     */
    private void classifyPacket(Packet packet, int size) {
        TcpPacket tcpPacket = packet.get(TcpPacket.class);
        UdpPacket udpPacket = packet.get(UdpPacket.class);
        
        if (tcpPacket != null) {
            classifyTcpPacket(tcpPacket, size);
        } else if (udpPacket != null) {
            classifyUdpPacket(udpPacket, size);
        } else {
            // Otros protocolos (ICMP, ARP, etc.)
            otherBytes.addAndGet(size);
        }
    }
    
    /**
     * Clasifica paquetes TCP
     */
    private void classifyTcpPacket(TcpPacket tcpPacket, int size) {
        int srcPort = tcpPacket.getHeader().getSrcPort().valueAsInt();
        int dstPort = tcpPacket.getHeader().getDstPort().valueAsInt();
        
        // HTTP/HTTPS y puertos web comunes
        if (isHttpPort(srcPort) || isHttpPort(dstPort)) {
            // La mayoría del tráfico web moderno (YouTube, Netflix, etc.) usa HTTPS/443
            httpBytes.addAndGet(size);
        }
        // Streaming tradicional sobre TCP
        else if (isStreamingPort(srcPort) || isStreamingPort(dstPort)) {
            streamingBytes.addAndGet(size);
        }
        // Puertos de aplicaciones de streaming conocidas
        else if (isKnownStreamingAppPort(srcPort) || isKnownStreamingAppPort(dstPort)) {
            streamingBytes.addAndGet(size);
        }
        // TCP para VoIP (SIP sobre TCP)
        else if (srcPort == 5060 || dstPort == 5060) {
            voipBytes.addAndGet(size);
        }
        // Heurística: Paquetes TCP grandes y constantes podrían ser streaming
        else if (size > 1400) {
            streamingBytes.addAndGet(size);
        }
        else {
            otherBytes.addAndGet(size);
        }
    }
    
    /**
     * Clasifica paquetes UDP
     */
    private void classifyUdpPacket(UdpPacket udpPacket, int size) {
        int srcPort = udpPacket.getHeader().getSrcPort().valueAsInt();
        int dstPort = udpPacket.getHeader().getDstPort().valueAsInt();
        
        // VoIP - RTP/RTCP (paquetes pequeños y frecuentes)
        if (isVoipPort(srcPort) || isVoipPort(dstPort)) {
            voipBytes.addAndGet(size);
        }
        // Heurística: UDP con paquetes pequeños (< 300 bytes) = probablemente VoIP
        else if (size < 300 && (srcPort >= 10000 || dstPort >= 10000)) {
            voipBytes.addAndGet(size);
        }
        // Streaming sobre UDP (QUIC, WebRTC, etc.)
        else if (isStreamingPort(srcPort) || isStreamingPort(dstPort)) {
            streamingBytes.addAndGet(size);
        }
        // QUIC (HTTP/3) - usado por YouTube, Google, Facebook
        else if (srcPort == 443 || dstPort == 443) {
            // QUIC usa UDP puerto 443
            httpBytes.addAndGet(size);
        }
        // Heurística: UDP con paquetes medianos/grandes = probablemente streaming
        else if (size > 500 && size < 1500) {
            streamingBytes.addAndGet(size);
        }
        // DNS, NTP y otros
        else if (srcPort == 53 || dstPort == 53 || srcPort == 123 || dstPort == 123) {
            otherBytes.addAndGet(size);
        }
        else {
            otherBytes.addAndGet(size);
        }
    }
    
    /**
     * Puertos HTTP/HTTPS comunes
     */
    private boolean isHttpPort(int port) {
        return port == 80 || port == 443 || port == 8080 || port == 8443 || 
               port == 3000 || port == 8000 || port == 8888;
    }
    
    /**
     * Puertos VoIP (SIP, RTP, RTCP)
     */
    private boolean isVoipPort(int port) {
        // SIP: 5060, 5061
        // RTP/RTCP: típicamente 16384-32767 (rango dinámico estándar)
        return port == 5060 || port == 5061 || 
               (port >= 16384 && port <= 32767) ||
               port == 3478 || port == 3479; // STUN/TURN para WebRTC
    }
    
    /**
     * Puertos de streaming tradicionales
     */
    private boolean isStreamingPort(int port) {
        // RTMP: 1935
        // RTSP: 554, 8554
        // HLS: varía pero usa HTTP
        // MPEG-DASH: usa HTTP
        return port == 1935 || port == 554 || port == 8554 || port == 1755;
    }
    
    /**
     * Puertos de aplicaciones de streaming conocidas
     */
    private boolean isKnownStreamingAppPort(int port) {
        // Spotify: 4070, 57621
        // Twitch: varios puertos altos
        // Discord voice: 50000-65535 (pero ya cubierto por VoIP)
        return port == 4070 || port == 57621;
    }
    
    /**
     * Calcula las velocidades por protocolo en MB/s
     */
    private void calculateProtocolSpeeds(long timeDiff) {
        long httpDiff = httpBytes.get() - lastHttpBytes;
        long voipDiff = voipBytes.get() - lastVoipBytes;
        long streamingDiff = streamingBytes.get() - lastStreamingBytes;
        long otherDiff = otherBytes.get() - lastOtherBytes;
        
        double timeInSeconds = timeDiff / 1000.0;
        
        currentProtocolStats.setHttpMbps((httpDiff / (1024.0 * 1024.0)) / timeInSeconds);
        currentProtocolStats.setVoipMbps((voipDiff / (1024.0 * 1024.0)) / timeInSeconds);
        currentProtocolStats.setStreamingMbps((streamingDiff / (1024.0 * 1024.0)) / timeInSeconds);
        currentProtocolStats.setOtherMbps((otherDiff / (1024.0 * 1024.0)) / timeInSeconds);
        
        lastHttpBytes = httpBytes.get();
        lastVoipBytes = voipBytes.get();
        lastStreamingBytes = streamingBytes.get();
        lastOtherBytes = otherBytes.get();
    }
    
    /**
     * Reinicia todos los contadores
     */
    private void resetCounters() {
        lastBytes = 0;
        lastTime = System.currentTimeMillis();
        currentSpeed = 0.0;
        totalBytes.set(0);
        
        httpBytes.set(0);
        voipBytes.set(0);
        streamingBytes.set(0);
        otherBytes.set(0);
        
        lastHttpBytes = 0;
        lastVoipBytes = 0;
        lastStreamingBytes = 0;
        lastOtherBytes = 0;
        
        currentProtocolStats = new Protocolo();
    }
    
    public void stopCapturing() {
        if (isCapturing.compareAndSet(true, false)) {
            cleanupHandles();
            
            if (captureThread != null) {
                captureThread.interrupt();
                captureThread = null;
            }
            
            currentSpeed = 0.0;
            currentProtocolStats = new Protocolo();
            broadcastTrafficData();
            logger.info("Captura de tráfico detenida");
        }
    }
    
    public void resetCounter() {
        resetCounters();
        broadcastTrafficData();
        logger.info("Contador de tráfico reiniciado");
    }
    
    public TrafficData getCurrentTrafficData() {
        return new TrafficData(currentSpeed, currentProtocolStats);
    }
    
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        
        try {
            TrafficData initialData = getCurrentTrafficData();
            String json = objectMapper.writeValueAsString(initialData);
            emitter.send(json);
            emitters.add(emitter);
        } catch (Exception e) {
            emitter.completeWithError(e);
            logger.error("Error al enviar datos iniciales al cliente SSE", e);
        }
        
        return emitter;
    }
    
    private void cleanupHandles() {
        for (PcapHandle handle : activeHandles) {
            try {
                handle.breakLoop();
                handle.close();
            } catch (Exception e) {
                logger.warn("Error al cerrar un handle de captura", e);
            }
        }
        activeHandles.clear();
    }
    
    private void broadcastTrafficData() {
        try {
            TrafficData data = getCurrentTrafficData();
            String json = objectMapper.writeValueAsString(data);
            
            List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
            
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(json);
                } catch (Exception e) {
                    deadEmitters.add(emitter);
                    logger.warn("Error al enviar actualización a un cliente SSE", e);
                }
            }
            
            emitters.removeAll(deadEmitters);
        } catch (Exception e) {
            logger.error("Error al serializar datos de tráfico", e);
        }
    }
    
    public static class TrafficData {
        private final double speed;
        private final Protocolo protocolo;
        
        public TrafficData(double speed, Protocolo protocolStats) {
            this.speed = speed;
            this.protocolo = protocolStats;
        }
        
        public double getSpeed() {
            return speed;
        }
        
        public Protocolo getProtocolStats() {
            return protocolo;
        }
    }
    
    @PostConstruct
    public void init() {
        logger.info("Servicio NetworkTraffic inicializado");
    }
    
    @PreDestroy
    public void cleanup() {
        stopCapturing();
        logger.info("Servicio NetworkTraffic detenido");
    }
}