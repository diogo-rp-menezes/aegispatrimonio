package br.com.aegispatrimonio.security;

import br.com.aegispatrimonio.model.RevokedToken;
import br.com.aegispatrimonio.repository.RevokedTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Denylist de tokens JWT revogados, persistida em banco (tabela
 * revoked_tokens, migration V18).
 *
 * Correção C1 do audit: a implementação anterior era in-memory
 * (ConcurrentHashMap) e não compartilhava revogações entre réplicas
 * k8s (aegis-app.yaml replicas: 2). Com persistência, qualquer réplica
 * vê a revogação.
 *
 * Indexação por jti (claim do JWT), não pela string completa do token;
 * entradas carregam o uid do usuário para permitir revogação em massa
 * (revokeAllForUser).
 *
 * Cada entrada vive até o timestamp de expiração (exp) do próprio token;
 * após isso a entrada é removida (eviction lazy no acesso + periódico via
 * @Scheduled), de modo que a tabela não cresce indefinidamente.
 */
@Service
@Slf4j
public class TokenDenylistService {

    private final RevokedTokenRepository revokedTokenRepository;

    public TokenDenylistService(RevokedTokenRepository revokedTokenRepository) {
        this.revokedTokenRepository = revokedTokenRepository;
    }

    /**
     * Revoga o token (por jti) até seu timestamp de expiração (epoch millis).
     * Idempotente: revogar duas vezes não tem efeito adicional (chave
     * primária jti; save sobrescreve a mesma entrada).
     */
    @Transactional
    public void revoke(String jti, long expiresAtEpochMillis, Long usuarioId) {
        if (jti == null || jti.isBlank()) {
            log.warn("Tentativa de revogar token sem jti; ignorada.");
            return;
        }
        if (expiresAtEpochMillis <= System.currentTimeMillis()) {
            // Token já expirado: nada a armazenar (evita entradas inúteis)
            return;
        }
        revokedTokenRepository.save(new RevokedToken(jti, usuarioId, expiresAtEpochMillis));
        log.info("Token JWT revogado via logout. jti={}, expira em {} ms (epoch).", jti, expiresAtEpochMillis);
    }

    /**
     * Verifica se o token (por jti) está revogado e ainda dentro da validade
     * da revogação. Eviction lazy: entradas expiradas são removidas no acesso.
     */
    @Transactional
    public boolean isRevoked(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        return revokedTokenRepository.findById(jti)
                .map(entry -> {
                    if (entry.getExpiresAt() <= System.currentTimeMillis()) {
                        revokedTokenRepository.delete(entry);
                        revokedTokenRepository.flush();
                        return false;
                    }
                    return true;
                })
                .orElse(false);
    }

    /**
     * Revoga todos os tokens emitidos para um usuário (por uid).
     * Tokens sem registro na denylist expiram naturalmente; tokens já
     * revogados permanecem (idempotente).
     *
     * @param usuarioId id do usuário (claim uid)
     * @param expiresAtEpochMillis validade máxima da revogação (tipicamente
     *        o maior exp entre os tokens ativos, ou now + jwtExpiration)
     */
    @Transactional
    public void revokeAllForUser(Long usuarioId, long expiresAtEpochMillis) {
        if (usuarioId == null) {
            log.warn("Tentativa de revogar tokens de usuário com uid nulo; ignorada.");
            return;
        }
        if (expiresAtEpochMillis <= System.currentTimeMillis()) {
            return;
        }
        List<RevokedToken> ativos = revokedTokenRepository.findByUsuarioId(usuarioId).stream()
                .filter(t -> t.getExpiresAt() > System.currentTimeMillis())
                .toList();
        for (RevokedToken t : ativos) {
            if (t.getExpiresAt() < expiresAtEpochMillis) {
                t.setExpiresAt(expiresAtEpochMillis);
                revokedTokenRepository.save(t);
            }
        }
        log.info("Revogação por usuário aplicada: uid={}, tokens afetados={}.", usuarioId, ativos.size());
    }

    /**
     * Eviction periódico de entradas expiradas (a cada 15 min).
     * Redundante com o eviction lazy, mas garante limpeza mesmo sem acesso.
     */
    @Scheduled(fixedDelay = 15 * 60 * 1000)
    @Transactional
    public void evictExpired() {
        int removed = revokedTokenRepository.deleteExpired(System.currentTimeMillis());
        if (removed > 0) {
            log.info("Denylist: {} tokens expirados removidos na limpeza periódica.", removed);
        }
    }

    /** Apenas para testes/observabilidade. */
    @Transactional(readOnly = true)
    public long size() {
        return revokedTokenRepository.count();
    }
}
