package com.example.security.servicios;

import com.example.security.modelo.Evento;
import com.example.security.dto.EventoDTO;
import com.example.security.modelo.Usuario;
import com.example.security.repositorio.EventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class EventoService {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private HttpServletRequest request;

    /**
     * Devuelve todos los eventos
     */
    public List<Evento> findAll() {
        return eventoRepository.findAll();
    }

    /**
     * Registra un nuevo evento con la IP del cliente
     */
    public Evento registrarEvento(EventoDTO eventoDTO, Usuario usuarioAutenticado) {
        Evento evento = new Evento();

        // Obtener IP del cliente
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }

        evento.setIp(ipAddress);
        evento.setFecha(new Date());
        evento.setDescripcion(eventoDTO.getDescripcion());
        evento.setUsuario(usuarioAutenticado);

        return eventoRepository.save(evento);
    }

}
