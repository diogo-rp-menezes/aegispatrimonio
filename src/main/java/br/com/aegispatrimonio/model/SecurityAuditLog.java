package br.com.aegispatrimonio.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "security_audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String username;

    private String resource;

    private String action;

    private String context;

    @Column(nullable = false)
    private String outcome; // ALLOW / DENY

    @Column(columnDefinition = "TEXT")
    private String details;
}
