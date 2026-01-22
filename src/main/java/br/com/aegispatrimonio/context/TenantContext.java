package br.com.aegispatrimonio.context;

/**
 * Gerencia o contexto do Tenant (Filial) atual usando ThreadLocal.
 * Componente fundamental do "Synaptic Switching" (Isolamento de Memória).
 */
public class TenantContext {
    private static final ThreadLocal<Long> CURRENT_FILIAL = new ThreadLocal<>();

    public static void setFilialId(Long filialId) {
        CURRENT_FILIAL.set(filialId);
    }

    public static Long getFilialId() {
        return CURRENT_FILIAL.get();
    }

    public static void clear() {
        CURRENT_FILIAL.remove();
    }
}
