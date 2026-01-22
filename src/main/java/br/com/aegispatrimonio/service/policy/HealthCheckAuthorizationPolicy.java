package br.com.aegispatrimonio.service.policy;

import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.Usuario;

public interface HealthCheckAuthorizationPolicy {
    void assertCanUpdate(Usuario usuario, Ativo ativo);
}
