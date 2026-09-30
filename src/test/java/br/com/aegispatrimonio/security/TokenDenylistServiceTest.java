package br.com.aegispatrimonio.security;

import br.com.aegispatrimonio.model.RevokedToken;
import br.com.aegispatrimonio.repository.RevokedTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários da denylist persistida (C1/C2 do audit): agora com
 * repository mockado, indexação por jti e revogação por usuário (uid).
 */
@ExtendWith(MockitoExtension.class)
class TokenDenylistServiceTest {

    @Mock
    private RevokedTokenRepository repository;

    private TokenDenylistService denylist;

    @BeforeEach
    void setUp() {
        denylist = new TokenDenylistService(repository);
    }

    @Test
    @DisplayName("Token revogado deve constar como revogado até expirar")
    void revoke_deveMarcarTokenComoRevogado() {
        long expiresAt = System.currentTimeMillis() + 60_000;
        when(repository.findById("jti-a")).thenReturn(Optional.of(new RevokedToken("jti-a", 1L, expiresAt)));

        denylist.revoke("jti-a", expiresAt, 1L);

        verify(repository).save(any(RevokedToken.class));
        assertTrue(denylist.isRevoked("jti-a"));
        assertFalse(denylist.isRevoked("jti-b"));
    }

    @Test
    @DisplayName("Revogação é idempotente: revogar duas vezes salva a mesma entrada (PK jti)")
    void revoke_idempotente() {
        long expiresAt = System.currentTimeMillis() + 60_000;
        when(repository.findById("jti-a")).thenReturn(Optional.of(new RevokedToken("jti-a", 1L, expiresAt)));

        denylist.revoke("jti-a", expiresAt, 1L);
        denylist.revoke("jti-a", expiresAt, 1L);

        verify(repository, times(2)).save(any(RevokedToken.class));
        assertTrue(denylist.isRevoked("jti-a"));
    }

    @Test
    @DisplayName("Token já expirado não deve ser armazenado na denylist")
    void revoke_tokenJaExpirado_naoDeveArmazenar() {
        denylist.revoke("jti-exp", System.currentTimeMillis() - 1_000, 1L);

        verify(repository, never()).save(any());
        when(repository.findById("jti-exp")).thenReturn(Optional.empty());
        assertFalse(denylist.isRevoked("jti-exp"));
    }

    @Test
    @DisplayName("Eviction lazy: entrada expirada é removida no acesso e deixa de revogar")
    void isRevoked_entradaExpirada_deveEvictar() {
        long expiresAt = System.currentTimeMillis() - 1_000;
        when(repository.findById("jti-a")).thenReturn(Optional.of(new RevokedToken("jti-a", 1L, expiresAt)));

        assertFalse(denylist.isRevoked("jti-a"));
        verify(repository).delete(any(RevokedToken.class));
    }

    @Test
    @DisplayName("Eviction periódico: entradas expiradas são removidas pelo @Scheduled")
    void evictExpired_deveRemoverEntradasExpiradas() {
        when(repository.deleteExpired(anyLong())).thenReturn(3);

        denylist.evictExpired();

        verify(repository).deleteExpired(anyLong());
    }

    @Test
    @DisplayName("revokeAllForUser estende a validade das revogações ativas do usuário")
    void revokeAllForUser_deveEstenderRevogacoesAtivas() {
        long now = System.currentTimeMillis();
        RevokedToken ativo = new RevokedToken("jti-1", 42L, now + 1_000);
        RevokedToken expirado = new RevokedToken("jti-2", 42L, now - 1_000);
        when(repository.findByUsuarioId(42L)).thenReturn(List.of(ativo, expirado));

        denylist.revokeAllForUser(42L, now + 60_000);

        ArgumentCaptor<RevokedToken> captor = ArgumentCaptor.forClass(RevokedToken.class);
        verify(repository, times(1)).save(captor.capture());
        assertEquals("jti-1", captor.getValue().getJti());
        assertEquals(now + 60_000, captor.getValue().getExpiresAt());
    }

    @Test
    @DisplayName("revokeAllForUser com uid nulo é ignorado")
    void revokeAllForUser_uidNulo_ignorado() {
        denylist.revokeAllForUser(null, System.currentTimeMillis() + 60_000);
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("revoke com jti nulo/vazio é ignorado")
    void revoke_jtiNulo_ignorado() {
        denylist.revoke(null, System.currentTimeMillis() + 60_000, 1L);
        denylist.revoke("  ", System.currentTimeMillis() + 60_000, 1L);
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("isRevoked com jti nulo retorna false sem consultar o banco")
    void isRevoked_jtiNulo_retornaFalse() {
        assertFalse(denylist.isRevoked(null));
        verifyNoInteractions(repository);
    }
}
