package br.com.aegispatrimonio.dto;

import java.time.LocalDateTime;

public record SecurityAuditLogDTO(
    Long id,
    LocalDateTime timestamp,
    String username,
    String resource,
    String action,
    String context,
    String outcome,
    String details
) {}
