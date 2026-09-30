package br.com.aegispatrimonio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Entrada da denylist de tokens JWT revogados (logout / revogação por usuário).
 *
 * Indexada por jti (claim do JWT), não pela string completa do token, e
 * associada ao id do usuário (uid) para permitir revogação em massa.
 * Persistida em banco para funcionar em deploy multi-réplica (C1 do audit).
 */
@Entity
@Table(name = "revoked_tokens")
@Getter
@Setter
@NoArgsConstructor
public class RevokedToken {

    /** Claim jti do JWT revogado (chave primária). */
    @Id
    @Column(name = "jti", length = 64, nullable = false)
    private String jti;

    /** Id do usuário dono do token (claim uid). */
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    /** Timestamp de expiração da revogação (epoch millis do exp do token). */
    @Column(name = "expires_at", nullable = false)
    private Long expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public RevokedToken(String jti, Long usuarioId, long expiresAt) {
        this.jti = jti;
        this.usuarioId = usuarioId;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }
}
