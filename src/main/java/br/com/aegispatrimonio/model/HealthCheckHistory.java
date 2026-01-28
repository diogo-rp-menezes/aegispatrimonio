package br.com.aegispatrimonio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "health_check_history")
public class HealthCheckHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, length = 128)
    private String host;

    @Column(name = "cpu_usage", precision = 5, scale = 4)
    private BigDecimal cpuUsage;

    @Column(name = "mem_free_percent", precision = 5, scale = 4)
    private BigDecimal memFreePercent;

    @Column(columnDefinition = "TEXT")
    private String disks;

    @Column(columnDefinition = "TEXT")
    private String nets;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
