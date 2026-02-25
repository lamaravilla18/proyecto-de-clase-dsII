package com.example.security.repositorio;

import com.example.security.modelo.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    // Métodos adicionales si los necesitas
    List<Evento> findByIp(String ip);
    List<Evento> findByFechaBetween(Date fechaInicio, Date fechaFin);
    List<Evento> findByUsuarioId(Long usuarioId);
}

