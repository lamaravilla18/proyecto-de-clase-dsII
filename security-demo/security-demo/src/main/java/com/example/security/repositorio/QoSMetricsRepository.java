package com.example.security.repositorio;

import com.example.security.modelo.QoSMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QoSMetricsRepository extends JpaRepository<QoSMetrics, Long> {
    
    // Obtener métricas por IP
    List<QoSMetrics> findByTargetIpOrderByTimestampDesc(String targetIp);
    
    // Obtener métricas de las últimas N horas
    @Query("SELECT q FROM QoSMetrics q WHERE q.timestamp >= :since ORDER BY q.timestamp DESC")
    List<QoSMetrics> findRecentMetrics(LocalDateTime since);
    
    // Obtener métricas por protocolo
    List<QoSMetrics> findByProtocolTypeOrderByTimestampDesc(String protocolType);
    
    // Obtener las últimas N métricas
    List<QoSMetrics> findTop10ByOrderByTimestampDesc();
    
    // Promedio de MOS en las últimas 24 horas
    @Query("SELECT AVG(q.mosScore) FROM QoSMetrics q WHERE q.timestamp >= :since")
    Double getAverageMosScore(LocalDateTime since);

    // Obtener métricas durante llamadas activas
    @Query("SELECT q FROM QoSMetrics q WHERE q.duringActiveCall = true ORDER BY q.timestamp DESC")
    List<QoSMetrics> findMetricsDuringCalls();

    // Obtener métricas sin llamadas activas
    @Query("SELECT q FROM QoSMetrics q WHERE q.duringActiveCall = false ORDER BY q.timestamp DESC")
    List<QoSMetrics> findMetricsWithoutCalls();

    // Promedio de MOS durante llamadas activas (últimas 24 horas)
    @Query("SELECT AVG(q.mosScore) FROM QoSMetrics q WHERE q.duringActiveCall = true AND q.timestamp >= :since")
    Double getAverageMosDuringCalls(LocalDateTime since);

    // Promedio de MOS sin llamadas activas (últimas 24 horas)
    @Query("SELECT AVG(q.mosScore) FROM QoSMetrics q WHERE q.duringActiveCall = false AND q.timestamp >= :since")
    Double getAverageMosWithoutCalls(LocalDateTime since);

    // Top 100 métricas recientes (para comparación)
    List<QoSMetrics> findTop100ByOrderByTimestampDesc();
}