package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.HealthCheckHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HealthCheckHistoryRepository extends JpaRepository<HealthCheckHistory, Long> {

    Optional<HealthCheckHistory> findTopByOrderByCreatedAtDesc();

    Page<HealthCheckHistory> findByOrderByCreatedAtDesc(Pageable pageable);

    List<HealthCheckHistory> findByCreatedAtAfterAndCpuUsageGreaterThanOrCreatedAtAfterAndMemFreePercentLessThanOrderByCreatedAtDesc(
            LocalDateTime after1, BigDecimal cpuThreshold,
            LocalDateTime after2, BigDecimal memThreshold
    );
}
