package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.healthcheck.HealthCheckDTO;
import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.AtivoDetalheHardware;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.repository.AtivoDetalheHardwareRepository;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.service.manager.HealthCheckCollectionsManager;
import br.com.aegispatrimonio.service.policy.HealthCheckAuthorizationPolicy;
import br.com.aegispatrimonio.service.updater.HealthCheckUpdater;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HealthCheckService implements IHealthCheckService { // Implementando a interface

    private final AtivoRepository ativoRepository;
    private final AtivoDetalheHardwareRepository detalheHardwareRepository;
    private final CurrentUserProvider currentUserProvider;
    private final HealthCheckAuthorizationPolicy authorizationPolicy;
    private final HealthCheckUpdater healthCheckUpdater;
    private final HealthCheckCollectionsManager collectionsManager;

    public HealthCheckService(AtivoRepository ativoRepository,
                              AtivoDetalheHardwareRepository detalheHardwareRepository,
                              CurrentUserProvider currentUserProvider,
                              HealthCheckAuthorizationPolicy authorizationPolicy,
                              HealthCheckUpdater healthCheckUpdater,
                              HealthCheckCollectionsManager collectionsManager) {
        this.ativoRepository = ativoRepository;
        this.detalheHardwareRepository = detalheHardwareRepository;
        this.currentUserProvider = currentUserProvider;
        this.authorizationPolicy = authorizationPolicy;
        this.healthCheckUpdater = healthCheckUpdater;
        this.collectionsManager = collectionsManager;
    }

    @Override // Anotação de override
    @Transactional
    public void updateHealthCheck(Long ativoId, HealthCheckDTO healthCheckDTO) {
        Usuario usuarioLogado = currentUserProvider.getCurrentUsuario();
        Ativo ativo = ativoRepository.findByIdWithDetails(ativoId)
                .orElseThrow(() -> new EntityNotFoundException("Ativo não encontrado com ID: " + ativoId));

        authorizationPolicy.assertCanUpdate(usuarioLogado, ativo);

        DetalhesResult detalhesResult = findOrCreateDetalhes(ativo);
        AtivoDetalheHardware detalhes = detalhesResult.detalhes();
        boolean createdNow = detalhesResult.createdNow();

        healthCheckUpdater.updateScalars(ativo.getId(), detalhes, healthCheckDTO, createdNow);

        collectionsManager.replaceCollections(detalhes, healthCheckDTO);
    }

    private DetalhesResult findOrCreateDetalhes(Ativo ativo) {
        AtivoDetalheHardware detalhes = detalheHardwareRepository.findById(ativo.getId()).orElse(null);
        boolean createdNow = (detalhes == null);

        if (createdNow) {
            detalhes = new AtivoDetalheHardware();
            detalhes.setAtivo(ativo);
            detalhes.setId(ativo.getId());
            detalhes = detalheHardwareRepository.saveAndFlush(detalhes);
        }
        return new DetalhesResult(detalhes, createdNow);
    }

    private record DetalhesResult(AtivoDetalheHardware detalhes, boolean createdNow) {}
}
