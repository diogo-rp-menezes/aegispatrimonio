package br.com.aegispatrimonio.service.manager;

import br.com.aegispatrimonio.dto.healthcheck.AdaptadorRedeDTO;
import br.com.aegispatrimonio.dto.healthcheck.DiscoDTO;
import br.com.aegispatrimonio.dto.healthcheck.HealthCheckDTO;
import br.com.aegispatrimonio.dto.healthcheck.MemoriaDTO;
import br.com.aegispatrimonio.mapper.HealthCheckMapper;
import br.com.aegispatrimonio.model.AdaptadorRede;
import br.com.aegispatrimonio.model.AtivoDetalheHardware;
import br.com.aegispatrimonio.model.Disco;
import br.com.aegispatrimonio.model.Memoria;
import br.com.aegispatrimonio.repository.AdaptadorRedeRepository;
import br.com.aegispatrimonio.repository.DiscoRepository;
import br.com.aegispatrimonio.repository.MemoriaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultHealthCheckCollectionsManagerTest {

    @Mock
    private DiscoRepository discoRepository;
    @Mock
    private MemoriaRepository memoriaRepository;
    @Mock
    private AdaptadorRedeRepository adaptadorRedeRepository;
    @Mock
    private HealthCheckMapper mapper;

    @InjectMocks
    private DefaultHealthCheckCollectionsManager collectionsManager;

    @Test
    @DisplayName("Deve limpar e recriar coleções quando DTO tem dados")
    void replaceCollections_whenDtoHasData_shouldClearAndRecreate() {
        // Given
        AtivoDetalheHardware detalhes = new AtivoDetalheHardware();
        detalhes.setId(1L);

        // Ajustando as chamadas de construtor dos DTOs para corresponderem às assinaturas atuais
        HealthCheckDTO dto = new HealthCheckDTO(
                null, null, null, null, null, null, null, null, null, null, null,
                List.of(new DiscoDTO("modelo1", "serial1", "tipo1", BigDecimal.ZERO, BigDecimal.ZERO, 0)), // Corrigido
                List.of(new MemoriaDTO("fabricante1", "serial1", "partNumber1", 0)), // Corrigido
                List.of(new AdaptadorRedeDTO("descricao1", "mac1", "127.0.0.1")) // Corrigido
        );

        when(mapper.toEntity(any(DiscoDTO.class))).thenReturn(new Disco());
        when(mapper.toEntity(any(MemoriaDTO.class))).thenReturn(new Memoria());
        when(mapper.toEntity(any(AdaptadorRedeDTO.class))).thenReturn(new AdaptadorRede());

        // When
        collectionsManager.replaceCollections(detalhes, dto);

        // Then
        // Verify cleaning
        verify(discoRepository, times(1)).deleteByAtivoDetalheHardwareId(detalhes.getId());
        verify(memoriaRepository, times(1)).deleteByAtivoDetalheHardwareId(detalhes.getId());
        verify(adaptadorRedeRepository, times(1)).deleteByAtivoDetalheHardwareId(detalhes.getId());

        // Verify mapping and recreation
        verify(mapper, times(1)).toEntity(any(DiscoDTO.class));
        verify(mapper, times(1)).toEntity(any(MemoriaDTO.class));
        verify(mapper, times(1)).toEntity(any(AdaptadorRedeDTO.class));

        verify(discoRepository, times(1)).saveAll(anyList());
        verify(memoriaRepository, times(1)).saveAll(anyList());
        verify(adaptadorRedeRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve apenas limpar coleções quando DTO tem listas vazias")
    void replaceCollections_whenDtoHasEmptyLists_shouldOnlyClear() {
        // Given
        AtivoDetalheHardware detalhes = new AtivoDetalheHardware();
        detalhes.setId(1L);

        HealthCheckDTO dto = new HealthCheckDTO(
                null, null, null, null, null, null, null, null, null, null, null,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList()
        );

        // When
        collectionsManager.replaceCollections(detalhes, dto);

        // Then
        // Verify cleaning
        verify(discoRepository, times(1)).deleteByAtivoDetalheHardwareId(detalhes.getId());
        verify(memoriaRepository, times(1)).deleteByAtivoDetalheHardwareId(detalhes.getId());
        verify(adaptadorRedeRepository, times(1)).deleteByAtivoDetalheHardwareId(detalhes.getId());

        // Verify no recreation
        verify(discoRepository, never()).saveAll(any());
        verify(memoriaRepository, never()).saveAll(any());
        verify(adaptadorRedeRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Deve apenas limpar coleções quando DTO tem listas nulas")
    void replaceCollections_whenDtoHasNullLists_shouldOnlyClear() {
        // Given
        AtivoDetalheHardware detalhes = new AtivoDetalheHardware();
        detalhes.setId(1L);

        HealthCheckDTO dto = new HealthCheckDTO(
                null, null, null, null, null, null, null, null, null, null, null,
                null, null, null
        );

        // When
        collectionsManager.replaceCollections(detalhes, dto);

        // Then
        // Verify cleaning
        verify(discoRepository, times(1)).deleteByAtivoDetalheHardwareId(detalhes.getId());
        verify(memoriaRepository, times(1)).deleteByAtivoDetalheHardwareId(detalhes.getId());
        verify(adaptadorRedeRepository, times(1)).deleteByAtivoDetalheHardwareId(detalhes.getId());

        // Verify no recreation
        verify(discoRepository, never()).saveAll(any());
        verify(memoriaRepository, never()).saveAll(any());
        verify(adaptadorRedeRepository, never()).saveAll(any());
    }
}
