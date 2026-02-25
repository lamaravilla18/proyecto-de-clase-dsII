package com.example.security.controller;

import com.example.security.modelo.Evento;
import com.example.security.dto.EventoDTO;
import com.example.security.servicios.EventoService;
import com.example.security.modelo.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventoController {

    @Autowired
    private EventoService eventoService;

    /**
     * Obtiene todos los logs de eventos
     */
    @GetMapping
    public ResponseEntity<List<Evento>> getAllEvents() {
        List<Evento> eventos = eventoService.findAll();
        return new ResponseEntity<>(eventos, HttpStatus.OK);
    }

    /**
     * Registra un nuevo evento
     */
    @PostMapping("/registre")
    public ResponseEntity<Evento> registrarEvento(@RequestBody EventoDTO eventoDTO, Usuario usuarioAutenticado) {
        Evento nuevoEvento = eventoService.registrarEvento(eventoDTO, usuarioAutenticado);
        return new ResponseEntity<>(nuevoEvento, HttpStatus.CREATED);
    }

}