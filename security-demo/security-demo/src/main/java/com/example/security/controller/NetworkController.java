package com.example.security.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.security.modelo.Evento;
import com.example.security.modelo.PingRequest;
import com.example.security.modelo.PingResponse;
import com.example.security.modelo.PortScanRequest;
import com.example.security.modelo.PortScanResponse;
import com.example.security.modelo.Usuario;
import com.example.security.repositorio.EventoRepository;
import com.example.security.servicios.NetworkService;
import com.example.security.servicios.UserService;

@RestController
@RequestMapping("/api")
public class NetworkController {

    @Autowired
    private NetworkService networkService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private EventoRepository eventoRepository;
    
    @PostMapping("/ping")
    public ResponseEntity<PingResponse> executePing(@RequestBody PingRequest request, Principal principal) {
    String username = principal.getName();
    Usuario usuario = userService.findByUsername(username);
    PingResponse response = networkService.executePing(request, usuario);
    return ResponseEntity.ok(response);
}

    @PostMapping("/scan")
    public ResponseEntity<PortScanResponse> executePortScan(@RequestBody PortScanRequest request, Principal principal) {
        String username = principal.getName();
        Usuario usuario = userService.findByUsername(username);
        PortScanResponse response = networkService.scanPorts(request, usuario);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/network-events")
    public ResponseEntity<List<Evento>> getAllEvents() {
    List<Evento> events = networkService.getAllEvents();
    return ResponseEntity.ok(events);
    }
    
    @GetMapping("/mis-pings")
    public ResponseEntity<List<Evento>> getMisPings(Principal principal) {
    String username = principal.getName();
    Usuario usuario = userService.findByUsername(username);
    List<Evento> eventos = eventoRepository.findByUsuarioId(usuario.getId());
    return ResponseEntity.ok(eventos);
}

}
