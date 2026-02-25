package com.example.security.repositorio;

import com.example.security.modelo.Dispositivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DispositivoRepository extends JpaRepository<Dispositivo, Long> {
    
    Dispositivo findByIp(String ip);
}