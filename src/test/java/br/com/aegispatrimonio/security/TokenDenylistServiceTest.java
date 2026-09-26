package br.com.aegispatrimonio.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenDenylistServiceTest {

    @Test
    @DisplayName("Token revogado deve constar como revogado até expirar")
    void revoke_deveMarcarTokenComoRevogado() {
        TokenDenylistService denylist = new TokenDenylistService();
        long expiresAt = System.currentTimeMillis() + 60_000;

        denylist.revoke("token-a", expiresAt);

        assertTrue(denylist.isRevoked("token-a"));
        assertFalse(denylist.isRevoked("token-b"));
    }

    @Test
    @DisplayName("Revogação é idempotente: revogar duas vezes não altera o resultado")
    void revoke_idempotente() {
        TokenDenylistService denylist = new TokenDenylistService();
        long expiresAt = System.currentTimeMillis() + 60_000;

        denylist.revoke("token-a", expiresAt);
        denylist.revoke("token-a", expiresAt);

        assertTrue(denylist.isRevoked("token-a"));
        assertEquals(1, denylist.size());
    }

    @Test
    @DisplayName("Token já expirado não deve ser armazenado na denylist")
    void revoke_tokenJaExpirado_naoDeveArmazenar() {
        TokenDenylistService denylist = new TokenDenylistService();

        denylist.revoke("token-exp", System.currentTimeMillis() - 1_000);

        assertFalse(denylist.isRevoked("token-exp"));
        assertEquals(0, denylist.size());
    }

    @Test
    @DisplayName("Eviction lazy: entrada expirada é removida no acesso e deixa de revogar")
    void isRevoked_entradaExpirada_deveEvictar() {
        TokenDenylistService denylist = new TokenDenylistService();
        long expiresAt = System.currentTimeMillis() + 50;

        denylist.revoke("token-a", expiresAt);
        assertTrue(denylist.isRevoked("token-a"));

        // Espera a revogação expirar
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertFalse(denylist.isRevoked("token-a"));
        assertEquals(0, denylist.size());
    }

    @Test
    @DisplayName("Eviction periódico: entradas expiradas são removidas pelo @Scheduled")
    void evictExpired_deveRemoverEntradasExpiradas() {
        TokenDenylistService denylist = new TokenDenylistService();
        denylist.revoke("token-velho", System.currentTimeMillis() - 1_000);
        denylist.revoke("token-novo", System.currentTimeMillis() + 60_000);

        denylist.evictExpired();

        assertEquals(1, denylist.size());
        assertFalse(denylist.isRevoked("token-velho"));
        assertTrue(denylist.isRevoked("token-novo"));
    }
}
