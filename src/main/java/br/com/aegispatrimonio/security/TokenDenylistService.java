package br.com.aegispatrimonio.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Denylist in-memory de tokens JWT revogados via logout.
 *
 * Cada entrada vive até o timestamp de expiração (exp) do próprio token;
 * após isso a entrada é removida (eviction lazy no acesso + periódico via
 * @Scheduled), de modo que a estrutura não cresce indefinidamente.
 *
 * LIMITAÇÃO: adequada para single-instance. Em deploy multi-réplica (ex.: k8s
 * com 2 réplicas), a revogação NÃO é compartilhada entre instâncias — migrar
 * para Redis/DB em follow-up.
 */
@Service
@Slf4j
public class TokenDenylistService {

    private final Map<String, Long> revokedTokens = new ConcurrentHashMap<>();

    /**
     * Revoga o token até seu timestamp de expiração (epoch millis).
     * Idempotente: revogar duas vezes não tem efeito adicional.
     */
    public void revoke(String token, long expiresAtEpochMillis) {
        if (expiresAtEpochMillis <= System.currentTimeMillis()) {
            // Token já expirado: nada a armazenar (evita entradas inúteis)
            return;
        }
        revokedTokens.put(token, expiresAtEpochMillis);
        log.info("Token JWT revogado via logout. Expira em {} ms (epoch).", expiresAtEpochMillis);
    }

    /**
     * Verifica se o token está revogado (e ainda dentro da validade da revogação).
     * Eviction lazy: entradas expiradas são removidas no acesso.
     */
    public boolean isRevoked(String token) {
        Long expiresAt = revokedTokens.get(token);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt <= System.currentTimeMillis()) {
            revokedTokens.remove(token, expiresAt);
            return false;
        }
        return true;
    }

    /**
     * Eviction periódico de entradas expiradas (a cada 15 min).
     * Redundante com o eviction lazy, mas garante limpeza mesmo sem acesso.
     */
    @Scheduled(fixedDelay = 15 * 60 * 1000)
    public void evictExpired() {
        long now = System.currentTimeMillis();
        int before = revokedTokens.size();
        revokedTokens.entrySet().removeIf(entry -> entry.getValue() <= now);
        int removed = before - revokedTokens.size();
        if (removed > 0) {
            log.info("Denylist: {} tokens expirados removidos na limpeza periódica.", removed);
        }
    }

    /** Apenas para testes/observabilidade. */
    int size() {
        return revokedTokens.size();
    }
}
