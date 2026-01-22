package br.com.aegispatrimonio.service.manager;

import br.com.aegispatrimonio.dto.healthcheck.HealthCheckDTO;
import br.com.aegispatrimonio.mapper.HealthCheckMapper;
import br.com.aegispatrimonio.model.AdaptadorRede;
import br.com.aegispatrimonio.model.AtivoDetalheHardware;
import br.com.aegispatrimonio.model.Disco;
import br.com.aegispatrimonio.model.Memoria;
import br.com.aegispatrimonio.repository.AdaptadorRedeRepository;
import br.com.aegispatrimonio.repository.DiscoRepository;
import br.com.aegispatrimonio.repository.MemoriaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DefaultHealthCheckCollectionsManager implements HealthCheckCollectionsManager {

    private final DiscoRepository discoRepository;
    private final MemoriaRepository memoriaRepository;
    private final AdaptadorRedeRepository adaptadorRedeRepository;
    private final HealthCheckMapper mapper;

    public DefaultHealthCheckCollectionsManager(DiscoRepository discoRepository,
                                              MemoriaRepository memoriaRepository,
                                              AdaptadorRedeRepository adaptadorRedeRepository,
                                              HealthCheckMapper mapper) {
        this.discoRepository = discoRepository;
        this.memoriaRepository = memoriaRepository;
        this.adaptadorRedeRepository = adaptadorRedeRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void replaceCollections(AtivoDetalheHardware detalhes, HealthCheckDTO dto) {
        final Long detalhesId = detalhes.getId();

        // 1. Limpar coleções existentes
        discoRepository.deleteByAtivoDetalheHardwareId(detalhesId);
        memoriaRepository.deleteByAtivoDetalheHardwareId(detalhesId);
        adaptadorRedeRepository.deleteByAtivoDetalheHardwareId(detalhesId);

        // 2. Recriar coleções a partir do DTO
        if (dto.discos() != null && !dto.discos().isEmpty()) {
            List<Disco> discos = dto.discos().stream()
                    .map(mapper::toEntity)
                    .peek(d -> d.setAtivoDetalheHardware(detalhes))
                    .collect(Collectors.toList());
            discoRepository.saveAll(discos);
        }

        if (dto.memorias() != null && !dto.memorias().isEmpty()) {
            List<Memoria> memorias = dto.memorias().stream()
                    .map(mapper::toEntity)
                    .peek(m -> m.setAtivoDetalheHardware(detalhes))
                    .collect(Collectors.toList());
            memoriaRepository.saveAll(memorias);
        }

        if (dto.adaptadoresRede() != null && !dto.adaptadoresRede().isEmpty()) {
            List<AdaptadorRede> adaptadores = dto.adaptadoresRede().stream()
                    .map(mapper::toEntity)
                    .peek(a -> a.setAtivoDetalheHardware(detalhes))
                    .collect(Collectors.toList());
            adaptadorRedeRepository.saveAll(adaptadores);
        }
    }
}
