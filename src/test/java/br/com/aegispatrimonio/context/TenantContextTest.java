package br.com.aegispatrimonio.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldSetAndGetFilialId() {
        Long filialId = 123L;
        TenantContext.setFilialId(filialId);
        assertEquals(filialId, TenantContext.getFilialId());
    }

    @Test
    void shouldClearFilialId() {
        TenantContext.setFilialId(123L);
        TenantContext.clear();
        assertNull(TenantContext.getFilialId());
    }
}
