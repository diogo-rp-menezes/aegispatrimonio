package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.SecurityAuditLogDTO;
import br.com.aegispatrimonio.model.SecurityAuditLog;
import br.com.aegispatrimonio.repository.SecurityAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityAuditService {

    private final SecurityAuditLogRepository repository;
    private final io.micrometer.core.instrument.MeterRegistry meterRegistry;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAuthorization(String username, String resource, String action, String context, boolean allowed, String details) {
        try {
            SecurityAuditLog auditLog = SecurityAuditLog.builder()
                    .timestamp(LocalDateTime.now())
                    .username(username)
                    .resource(resource)
                    .action(action)
                    .context(context)
                    .outcome(allowed ? "ALLOW" : "DENY")
                    .details(details)
                    .build();

            repository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to persist security audit log", e);
            meterRegistry.counter("aegis_audit_log_failure").increment();
        }
    }

    @Transactional(readOnly = true)
    public Page<SecurityAuditLogDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(log -> new SecurityAuditLogDTO(
                        log.getId(),
                        log.getTimestamp(),
                        log.getUsername(),
                        log.getResource(),
                        log.getAction(),
                        log.getContext(),
                        log.getOutcome(),
                        log.getDetails()
                ));
    }
}
